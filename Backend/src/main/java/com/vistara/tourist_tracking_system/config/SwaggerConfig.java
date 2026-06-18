package com.vistara.tourist_tracking_system.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().removeIf(p ->
                        p.getName().equalsIgnoreCase("userDetails") ||
                                p.getName().equalsIgnoreCase("principal") ||
                                p.getName().equalsIgnoreCase("authentication")
                );
            }
            return operation;
        };
    }
}