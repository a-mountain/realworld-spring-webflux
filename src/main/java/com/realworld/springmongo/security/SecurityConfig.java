package com.realworld.springmongo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationWebFilter webFilter, EndpointsSecurityConfig endpointsConfig) {
        var authorizeExchange = http.authorizeExchange();
        return endpointsConfig.apply(authorizeExchange)
                .and()
                .addFilterAt(webFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .build();
    }

    @Bean
    EndpointsSecurityConfig endpointsConfig() {
        return http -> http
                .pathMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/profiles/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                .anyExchange().authenticated();
    }

    @FunctionalInterface
    interface EndpointsSecurityConfig {
        ServerHttpSecurity.AuthorizeExchangeSpec apply(ServerHttpSecurity.AuthorizeExchangeSpec http);
    }
}
