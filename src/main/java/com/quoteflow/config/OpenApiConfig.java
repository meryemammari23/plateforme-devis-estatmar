package com.quoteflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quoteFlowOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("QuoteFlow - Module Meryem")
                .version("0.0.1")
                .description("Demandes de devis, generation PDF (recap + devis) et integration n8n."));
    }
}
