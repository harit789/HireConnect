package com.hireconnect.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info().title("HireConnect — User Service API")
						.description(
								"Handles authentication, JWT, GitHub OAuth2, candidate & recruiter profile management")
						.version("v1.0")
						.contact(new Contact().name("HireConnect Team").email("support@hireconnect.com"))
						.license(new License().name("MIT License")))
				.servers(List.of(new Server().url("http://localhost:8081").description("Local Development Server")))
				// JWT Bearer auth button in Swagger UI
				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new Components().addSecuritySchemes("Bearer Authentication",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
								.description("Enter your JWT token")));
	}
}