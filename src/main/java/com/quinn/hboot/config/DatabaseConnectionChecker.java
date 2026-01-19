/*
package com.quinn.hboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionChecker {

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void checkConnection() {
        try {
            Connection conn = dataSource.getConnection();
            System.out.println("数据库连接成功!");
            conn.close();
        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage());
//            throw new RuntimeException("数据库连接失败", e);
        }
    }
}
*/
