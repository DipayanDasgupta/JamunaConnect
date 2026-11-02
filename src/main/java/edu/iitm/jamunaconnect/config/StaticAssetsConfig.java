package edu.iitm.jamunaconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Backend-rendered pages are also served through the Vercel proxy, where
 * /css and /js belong to the static site. Give the server-rendered pages
 * their own asset namespace so they always load the backend's own files.
 */
@Configuration
public class StaticAssetsConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/staff-assets/**")
                .addResourceLocations("classpath:/static/");
    }
}
