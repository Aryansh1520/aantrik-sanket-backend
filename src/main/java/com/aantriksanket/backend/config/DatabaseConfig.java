package com.aantriksanket.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    // Spring Boot auto-configures DataSource using application.properties
}
