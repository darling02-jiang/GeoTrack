package com.geotrack.content.config;

import com.geotrack.common.guard.InterfaceGuardService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class InterfaceGuardConfig {

    @Bean
    public InterfaceGuardService interfaceGuardService(StringRedisTemplate redisTemplate) {
        return new InterfaceGuardService(redisTemplate);
    }
}
