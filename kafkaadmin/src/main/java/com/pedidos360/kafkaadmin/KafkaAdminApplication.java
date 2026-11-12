package com.pedidos360.kafkaadmin;

import com.pedidos360.kafkaadmin.config.AdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
public class KafkaAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaAdminApplication.class, args);
    }
}