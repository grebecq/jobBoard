package fedoseev.jobboard.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey signKey;

    @PostConstruct
    void initSignKey() {
        if (secret == null || secret.isBlank()) {
            signKey = Jwts.SIG.HS256.key().build();
            log.warn("jwt.secret не задан — сгенерирован временный ключ. " +
                    "Выданные токены перестанут действовать после перезапуска. " +
                    "Задайте переменную окружения JWT_SECRET (base64, минимум 32 байта).");
            return;
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret короче 256 бит — для HS256 нужно минимум 32 байта");
        }
        signKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSignKey() {
        return signKey;
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        final String username = extractUsername(token);
        return username.equals(email) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Date exp = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return exp.before(new Date());
    }
}
