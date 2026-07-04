package com.example.invoiceplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI invoicePlatformOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Financial Invoice Analytics Platform API")
                .description("Invoice OCR ingestion and accounts-payable analytics")
                .version("v1"));
    }
}
