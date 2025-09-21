package com.hrms.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.hrms")
@EntityScan("com.hrms.entity")
@EnableJpaRepositories("com.hrms.repository")
public class HrManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrManagementSystemApplication.class, args);
    }
}