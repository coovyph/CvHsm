package com.company.cvhsm.hsm;

import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import com.company.cvhsm.exception.HsmException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Matches HSM responses to pending requests by correlation header and applies a per-request timeout.
 */
@Component
@Log4j2
public class ResponseRouter {

	private static final String TIMEOUT_THREAD_PREFIX = "hsm-response-timeout-";

	private final ConcurrentHashMap<String, PendingEntry> pending = new ConcurrentHashMap<>();
	private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor(
			new TimeoutThreadFactory());

	/**
	 * Registers a pending request and returns a future that is completed when the correlated response arrives
	 * or fails with a {@link TimeoutException} if the timeout expires first.
	 *
	 * @param correlationHeader the unique 4-char HSM header used as the correlation ID
	 * @param timeoutMillis     the maximum time to wait for a response
	 * @return a future that completes with the HSM response
	 */
	public CompletableFuture<HsmResponse> register(String correlationHeader, long timeoutMillis) {
		return register(correlationHeader,null,timeoutMillis);
	}

	/**
	 * Registers a pending request and returns a future that is completed when the correlated response arrives
	 * or fails with a {@link TimeoutException} if the timeout expires first.
	 *
	 * @param correlationHeader the unique 4-char HSM header used as the correlation ID
	 * @param inCache the cache informations, may useful on handling response
	 * @param timeoutMillis     the maximum time to wait for a response
	 * @return a future that completes with the HSM response
	 */
	public CompletableFuture<HsmResponse> register(String correlationHeader,Object inCache, long timeoutMillis) {
		CompletableFuture<HsmResponse> future = new CompletableFuture<>();

		ScheduledFuture<?> timeoutTask = timeoutExecutor.schedule(() -> {
			PendingEntry entry = pending.remove(correlationHeader);
			if (entry != null) {
				log.warn("HSM request with header {} timed out after {} ms", correlationHeader, timeoutMillis);
				entry.future.completeExceptionally(
						new TimeoutException("HSM request with header " + correlationHeader + " timed out"));
			}
		}, timeoutMillis, TimeUnit.MILLISECONDS);

		PendingEntry entry = new PendingEntry(future,inCache, timeoutTask);
		pending.put(correlationHeader, entry);

		return future;
	}


	public void complete(HsmResponse response) {
		PendingEntry entry = pending.remove(response.getHeader());
		if (entry != null) {
			cancelTimeout(entry);
			response.setCache(entry.cache);
			entry.future.complete(response);
			log.debug("Completed HSM response for header={}", response.getHeader());
		} else {
			log.warn("Received HSM response for unknown or timed out header={}", response.getHeader());
		}
	}

	public void fail(String correlationHeader, Throwable cause) {
		PendingEntry entry = pending.remove(correlationHeader);
		if (entry != null) {
			cancelTimeout(entry);
			entry.future.completeExceptionally(cause);
		}
	}

	public boolean hasPending(String correlationHeader) {
		return pending.containsKey(correlationHeader);
	}

	@PreDestroy
	public void shutdown() {
		log.info("Shutting down response router timeout executor");
		timeoutExecutor.shutdownNow();
	}

	private void cancelTimeout(PendingEntry entry) {
		if (entry.timeoutTask != null) {
			entry.timeoutTask.cancel(false);
		}
	}

	private static final class PendingEntry {
		final CompletableFuture<HsmResponse> future;
		final ScheduledFuture<?> timeoutTask;
		final Object cache;

		PendingEntry(CompletableFuture<HsmResponse> future,Object inCache, ScheduledFuture<?> timeoutTask) {
			this.future = future;
			this.timeoutTask = timeoutTask;
			this.cache = inCache;
		}
	}

	private static final class TimeoutThreadFactory implements ThreadFactory {
		private final AtomicInteger counter = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, TIMEOUT_THREAD_PREFIX + counter.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		}
	}
}
