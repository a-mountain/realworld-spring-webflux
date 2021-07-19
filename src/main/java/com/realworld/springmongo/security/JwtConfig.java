package com.realworld.springmongo.security;

import com.realworld.springmongo.user.UserTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    ServerAuthenticationConverter jwtServerAuthenticationConverter(TokenFormatter tokenFormatter) {
        return exchange -> Mono.justOrEmpty(exchange)
                .flatMap(ex -> Mono.justOrEmpty(getAuthorizationHeaders(exchange)))
                .filter(header -> !header.isEmpty())
                .map(headers -> tokenFormatter.getRowToken(headers.get(0)))
                .map(token -> new UsernamePasswordAuthenticationToken(token, token));

    }

    private List<String> getAuthorizationHeaders(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
    }

    @Bean
    ReactiveAuthenticationManager jwtAuthenticationManager(JwtSigner tokenService) {
        return authentication -> Mono.justOrEmpty(authentication).map(auth -> {
            var token = (String) auth.getCredentials();
            var jws = tokenService.validate(token);
            var authority = new SimpleGrantedAuthority(("ROLE_USER"));
            var userId = jws.getBody().getSubject();
            return new UsernamePasswordAuthenticationToken(
                    userId,
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

    @Bean
    UserTokenProvider userTokenProvider(TokenFormatter formatter, JwtSigner tokenService) {
        return userId -> {
            var token = tokenService.generateToken(userId);
            return formatter.formatToken(token);
        };
    }
}
