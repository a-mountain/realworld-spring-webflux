package com.realworld.springmongo.security;

import com.realworld.springmongo.user.UserTokenProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtSigner implements UserTokenProvider {

    private final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    private final JwtParser jwtParser = Jwts.parserBuilder()
            .setSigningKey(keyPair.getPublic())
            .build();
    private final JwtProperties jwtProperties;

    public Jws<Claims> validate(String jwt) {
        return jwtParser.parseClaimsJws(jwt);
    }

    public String generateToken(String userId) {
        return Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setSubject(userId)
                .setExpiration(expirationDate())
                .setIssuer("identity")
                .compact();
    }

    private Date expirationDate() {
        var expirationDate = System.currentTimeMillis() + jwtProperties.getSessionTime() * 1000L;
        return new Date(expirationDate);
    }

    @Override
    public String getToken(String userId) {
        return generateToken(userId);
    }
}
