package com.birthday.guardian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.birthday.guardian.mapper")
@EnableScheduling
public class BirthdayGuardianApplication {
    public static void main(String[] args) {
        SpringApplication.run(BirthdayGuardianApplication.class, args);
    }
}
