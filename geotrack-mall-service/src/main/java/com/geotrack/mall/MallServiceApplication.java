package com.geotrack.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.geotrack.mall", "com.geotrack.common.exception"})
@EnableFeignClients(basePackages = "com.geotrack.mall.client")
@EnableScheduling
@MapperScan("com.geotrack.mall.mapper")
public class MallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallServiceApplication.class, args);
    }
}
