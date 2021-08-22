package com.realworld.springmongo.security;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Value
@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
public class JwtProperties {
    int sessionTime;
}
