package com.hoxkar.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
/**
 * 权限管理服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"com.hoxkar"}, 
               excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                                                    classes = {com.hoxkar.common.aspect.PermissionAspect.class}))
public class PermissionServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PermissionServiceApplication.class, args);
    }
} 