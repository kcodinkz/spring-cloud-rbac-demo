package com.hoxkar.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Eureka服务注册中心安全配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // Eureka相关端点需要认证
                .requestMatchers("/eureka/**").authenticated()
                // 管理端点需要认证
                .requestMatchers("/actuator/**").authenticated()
                // 其他端点允许匿名访问
                .anyRequest().permitAll()
            )
            // 启用HTTP Basic认证
            .httpBasic(basic -> {});
        
        return http.build();
    }
} 