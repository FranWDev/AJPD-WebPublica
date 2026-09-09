package org.dubini.frontend_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.dubini.frontend_api.config.JwtProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long jwtExpiration;

    public JwtProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(props.getSecret()));
        this.jwtExpiration = 5000L;
    }

    public String generateToken() {
        return Jwts.builder()
                .subject("backoffice")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();
            Date now = new Date();

            return "backoffice".equals(subject) && expiration.after(now);
        } catch (Exception e) {
            return false;
        }
    }
}
