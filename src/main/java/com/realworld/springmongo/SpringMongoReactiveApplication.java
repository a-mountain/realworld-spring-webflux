package com.realworld.springmongo;

import com.realworld.springmongo.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication public class SpringMongoReactiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringMongoReactiveApplication.class, args);
    }
}
