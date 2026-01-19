package com.quinn.hboot;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class
})
public class HBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HBootApplication.class, args);

        // 打印一些有用的信息
        System.out.println("\n=========================================");
        System.out.println("H-Boot Application Started Successfully!");
        System.out.println("=========================================");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui/index.html");
        System.out.println("OpenAPI JSON: http://localhost:8080/v3/api-docs");
        System.out.println("=========================================\n");
    }

}
