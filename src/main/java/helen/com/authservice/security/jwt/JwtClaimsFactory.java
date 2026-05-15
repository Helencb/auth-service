package helen.com.authservice.security.jwt;

import helen.com.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtClaimsFactory {

    public Claims build(User user) {

        Claims claims = Jwts.claims();

        claims.put("roles",
                user.getRoles()
                        .stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
                );
        claims.setSubject(user.getUsername());
        claims.setIssuedAt(new Date());

        return claims;
    }
}
