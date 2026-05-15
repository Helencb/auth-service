package helen.com.authservice.security.jwt;

import helen.com.authservice.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final JwtConfig config;

    public boolean isValid(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(config.getSecret().getBytes());

            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
           return false;
        }
    }
}
