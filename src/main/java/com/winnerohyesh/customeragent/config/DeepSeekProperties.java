package com.winnerohyesh.customeragent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
@Data
public class DeepSeekProperties {

    private String apiKey = "";
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private Integer maxTokens = 2048;
    private Double temperature = 0.7;
    private Integer timeoutMs = 60000;
    private Boolean stream = true;
}
