package com.anurag.labslot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CsrfInterceptor csrf;
    public WebConfig(CsrfInterceptor csrf) { this.csrf = csrf; }
    @Override public void addInterceptors(@NonNull InterceptorRegistry registry) { registry.addInterceptor(csrf).addPathPatterns("/api/**"); }
}