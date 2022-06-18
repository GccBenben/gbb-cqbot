package com.gccbenben.qqbotservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PixivPictureConfig implements WebMvcConfigurer {

    @Value("${picture.pixiv.dir}")
    private String pixivImageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/pixiv/**")
                .addResourceLocations("file:" + pixivImageDir);
    }
}
