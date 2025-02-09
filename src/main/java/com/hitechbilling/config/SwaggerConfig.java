package com.hitechbilling.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customerOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("HiTech-Billing Documentation")
                        .description("API documentation for HiTech-Billing")
                        .contact(new Contact()
                                .name("codecrafterservices")
                                .email("info@code-crafters.in")
                                .url("https://code-crafter.in")
                        )
                );
    }
}
