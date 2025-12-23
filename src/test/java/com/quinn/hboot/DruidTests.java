package com.quinn.hboot;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Slf4j
class DruidTests {


    /**
     * Druid 密码加密
     */
    @Test
    @SneakyThrows
    void testEncodePassword() {
        // 你的密码
        String password = "123456";
        String[] arr = ConfigTools.genKeyPair(512);

        // 私钥
        log.info("privateKey: {}", arr[0]);
        // 公钥
        log.info("publicKey: {}", arr[1]);

        // 通过私钥加密密码
        String encodePassword = ConfigTools.encrypt(arr[0], password);
        log.info("password: {}", encodePassword);
//        2025-12-23T14:45:52.537+08:00  INFO 9644 --- [hboot-system] [           main] com.quinn.hboot.DruidTests               : privateKey: MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAp6FsSN+R46pjRGdA2ZxVL8VYSRlsOmJTvYMkTDx/MUQPCO4Xz06iHJCyGw33aGjmH8ZCcjPqHw8cDflvIWTGHwIDAQABAkAPuV0ictJJBE0ed/8FypvVCNJwvPFBgsKvJrOLcStPMBcaHtuCUcadnw1IbCwl/fFcTwJIBjxjg4IFPJqFfMfNAiEAq7XgEzLnWXabL0Q8bUPSxXGdhci5/YshcZprB6JgLRUCIQD56thQZMxamcWAMiSyl5m3EDZ/B3FewJ2Vw/awM3G7YwIge+b/S5s9qSaWkCLS351DJ8n1+qV+399XQJxGgvm+IIkCIQCDQqJMTzS5nSwLMcrudgtJB93niAw2XOonf94wG0TvywIhAJiAWZxNjVo2UmBvWveximpqhtvvTNfR7ux52ypQNDIk
//        2025-12-23T14:45:52.539+08:00  INFO 9644 --- [hboot-system] [           main] com.quinn.hboot.DruidTests               : publicKey: MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKehbEjfkeOqY0RnQNmcVS/FWEkZbDpiU72DJEw8fzFEDwjuF89OohyQshsN92ho5h/GQnIz6h8PHA35byFkxh8CAwEAAQ==
//                2025-12-23T14:45:52.560+08:00  INFO 9644 --- [hboot-system] [           main] com.quinn.hboot.DruidTests               : password: AfgaqHawW1eQAEeXhS6FFvhNXOO2MQFXLrGPWxCSznfPd8MCa+Nzs3xQGwqRsWlJ/u+6ck4vxZNmqiYSV2CGJg==
    }

}