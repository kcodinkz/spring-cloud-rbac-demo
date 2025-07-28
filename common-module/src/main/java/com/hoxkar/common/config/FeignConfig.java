package com.hoxkar.common.config;

import feign.Logger;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign客户端配置
 */
@Configuration
public class FeignConfig {
    
    /**
     * 配置Feign日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    /**
     * 配置Feign请求超时时间
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
            10, TimeUnit.SECONDS,  // 连接超时
            60, TimeUnit.SECONDS,  // 读取超时
            true                   // 跟随重定向
        );
    }
    
    /**
     * 配置Feign编码器
     */
    @Bean
    public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
    
    /**
     * 配置Feign解码器
     */
    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringDecoder(messageConverters);
    }
} 