package com.company.cvhsm;

import com.company.cvhsm.config.HsmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(HsmProperties.class)
@EnableScheduling
public class CvHsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvHsmApplication.class, args);
    }
}
