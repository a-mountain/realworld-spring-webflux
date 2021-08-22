package com.realworld.springmongo.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    ServerAuthenticationConverter jwtServerAuthenticationConverter(TokenExtractor tokenExtractor) {
        return ex -> Mono.justOrEmpty(ex).flatMap(exchange -> {
            var headers = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (headers == null || headers.isEmpty()) {
                return Mono.empty();
            }
            var authHeader = headers.get(0);
            var token = tokenExtractor.extractToken(authHeader);
            return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
        });
    }

    @Bean
    ReactiveAuthenticationManager jwtAuthenticationManager(JwtSigner tokenService) {
        return authentication -> Mono.justOrEmpty(authentication).map(auth -> {
            var token = (String) auth.getCredentials();
            var jws = tokenService.validate(token);
            var authority = new SimpleGrantedAuthority(("ROLE_USER"));
            var userId = jws.getBody().getSubject();
            var tokenPrincipal = new TokenPrincipal(userId, token);
            return new UsernamePasswordAuthenticationToken(
                    tokenPrincipal,
                    token,
                    List.of(authority)
            );
        });
    }

    @Bean
    AuthenticationWebFilter authenticationFilter(ReactiveAuthenticationManager manager, ServerAuthenticationConverter converter) {
        var authenticationWebFilter = new AuthenticationWebFilter(manager);
        authenticationWebFilter.setServerAuthenticationConverter(converter);
        return authenticationWebFilter;
    }
}
