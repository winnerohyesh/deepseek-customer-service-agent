package com.winnerohyesh.customeragent;

import com.winnerohyesh.customeragent.config.DeepSeekProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepSeekProperties.class)
public class CustomerAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerAgentApplication.class, args);
    }
}
