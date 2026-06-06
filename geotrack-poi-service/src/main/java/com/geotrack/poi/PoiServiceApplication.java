package com.geotrack.poi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.geotrack.poi", "com.geotrack.common.exception"})
@EnableFeignClients(basePackages = "com.geotrack.poi.client")
@MapperScan("com.geotrack.poi.mapper")
public class PoiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoiServiceApplication.class, args);
    }
}
