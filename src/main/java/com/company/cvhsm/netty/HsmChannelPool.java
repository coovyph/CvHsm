package com.company.cvhsm.netty;

import com.company.cvhsm.config.HsmProperties;
import com.company.cvhsm.hsm.ResponseRouter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a fixed-size pool of persistent TCP channels to the Thales HSM.
 *
 * <p>Channels are eagerly connected at startup. Disconnected channels are removed from the
 * active pool and scheduled for reconnection after a configurable delay.
 */
@Component
@Log4j2
public class HsmChannelPool {

    private final HsmProperties hsmProperties;
    private final ResponseRouter responseRouter;
    private final EventLoopGroup eventLoopGroup;
    private final List<Channel> activeChannels;
    private final AtomicInteger roundRobin;
    private final Object lifecycleLock = new Object();

    private Bootstrap bootstrap;
    private volatile boolean running = true;

    public HsmChannelPool(HsmProperties hsmProperties, ResponseRouter responseRouter) {
        this.hsmProperties = hsmProperties;
        this.responseRouter = responseRouter;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.activeChannels = new CopyOnWriteArrayList<>();
        this.roundRobin = new AtomicInteger(0);
    }

    @PostConstruct
    public void start() {
        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, hsmProperties.getConnectTimeoutMillis())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new HsmChannelInitializer(responseRouter));

        int sessions = hsmProperties.getMaxSessions();
        log.info("Eagerly establishing {} HSM session(s) to {}:{}",
                sessions, hsmProperties.getHost(), hsmProperties.getPort());

        for (int i = 0; i < sessions; i++) {
            connect();
        }
    }

    @PreDestroy
    public void stop() {
        synchronized (lifecycleLock) {
            running = false;
        }
        log.info("Shutting down HSM channel pool");
        for (Channel channel : activeChannels) {
            channel.close();
        }
        activeChannels.clear();
        eventLoopGroup.shutdownGracefully();
    }

    public Channel borrowChannel() {
        if (activeChannels.isEmpty()) {
            throw new IllegalStateException("No active HSM channels available");
        }

        int attempts = activeChannels.size();
        for (int i = 0; i < attempts; i++) {
            int index = roundRobin.getAndIncrement() % activeChannels.size();
            Channel channel = activeChannels.get(index);
            if (channel.isActive()) {
                log.info("Borrowing HSM channel {}", channel.id());
                return channel;
            }
            activeChannels.remove(channel);
            log.warn("Removed inactive HSM channel {} from pool", channel.id());
        }

        throw new IllegalStateException("No active HSM channels available after checking pool");
    }

    public int activeChannelCount() {
        return activeChannels.size();
    }

    /**
     * Returns a snapshot of the currently active channels in the pool.
     */
    public List<Channel> activeChannels() {
        return List.copyOf(activeChannels);
    }

    private void connect() {
        synchronized (lifecycleLock) {
            if (!running) {
                return;
            }
        }

        ChannelFuture future = bootstrap.connect(hsmProperties.getHost(), hsmProperties.getPort());
        future.addListener((ChannelFuture connectFuture) -> {
            if (connectFuture.isSuccess()) {
                Channel channel = connectFuture.channel();
                log.info("HSM channel {} connected", channel.id());
                activeChannels.add(channel);
                channel.closeFuture().addListener(closeFuture -> onChannelClosed(channel));
            } else {
                log.error("Failed to connect HSM channel to {}:{}",
                        hsmProperties.getHost(), hsmProperties.getPort(), connectFuture.cause());
                scheduleReconnect();
            }
        });
    }

    private void onChannelClosed(Channel channel) {
        activeChannels.remove(channel);

        synchronized (lifecycleLock) {
            if (!running) {
                log.info("HSM channel {} closed; reconnect skipped because pool is stopped", channel.id());
                return;
            }
        }

        log.warn("HSM channel {} closed; scheduling reconnect", channel.id());
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        synchronized (lifecycleLock) {
            if (!running) {
                return;
            }
        }

        long delayMillis = hsmProperties.getReconnectDelayMillis();
        log.info("Scheduling HSM reconnect in {} ms", delayMillis);
        eventLoopGroup.schedule(this::connect, delayMillis, TimeUnit.MILLISECONDS);
    }
}
