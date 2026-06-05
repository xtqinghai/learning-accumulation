package com.quinn.hboot;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DruidDataSourceAutoConfigure.class
})
public class HBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HBootApplication.class, args);
        log.info("H-Boot application started");
        log.info("Swagger UI: http://localhost:8080/swagger-ui/index.html");
        log.info("OpenAPI JSON: http://localhost:8080/v3/api-docs");
    }
}
