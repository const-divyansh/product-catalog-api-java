package com.example.catalog.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productCatalogOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Product Catalog REST API")
                .description("Production-grade Spring Boot 3 REST API for an e-commerce product catalog designed for high read throughput, complex filtering, and pagination.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Product Catalog Team")
                    .email("support@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
