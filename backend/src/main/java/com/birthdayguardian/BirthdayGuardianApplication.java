package com.birthdayguardian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 生日守护者应用启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.birthdayguardian.mapper")
public class BirthdayGuardianApplication {

    public static void main(String[] args) {
        SpringApplication.run(BirthdayGuardianApplication.class, args);
    }
}