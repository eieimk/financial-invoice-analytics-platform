package com.example.invoiceplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InvoicePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoicePlatformApplication.class, args);
    }
}
