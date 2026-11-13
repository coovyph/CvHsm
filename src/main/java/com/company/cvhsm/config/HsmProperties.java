package com.company.cvhsm.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "hsm")
@Validated
@Data
public class HsmProperties {

    @NotBlank
    private String host = "localhost";

    @Min(1)
    @Max(65535)
    private int port = 9999;

    @Min(1)
    @Max(100)
    private int maxSessions = 4;

    @Min(100)
    private int connectTimeoutMillis = 5000;

    @Min(100)
    private int requestTimeoutMillis = 5000;

    @Min(100)
    private int reconnectDelayMillis = 5000;

    @NotNull
    private Echo echo = new Echo();

    @Data
    public static class Echo {
        private boolean enabled = true;

        @Min(1000)
        private long intervalMillis = 30000;
    }
}
