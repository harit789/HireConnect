package com.hireconnect.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Serves uploaded resume files as static resources.
     * GET /uploads/{filename} → reads from the local filesystem upload directory.
     * This allows the browser to download/view resumes directly from the backend.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:f:/HireConnect-Resume/uploads/");
    }
}
