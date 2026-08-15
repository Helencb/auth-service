package helen.com.authservice.security.jwt;

import helen.com.authservice.entity.User;
import helen.com.authservice.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class JwtClaimsFactory {

    public Claims build(User user) {

        return Jwts.claims()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .add("roles",
                        user.getRoles()
                                .stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toList())
                )
                // Single, gateway-friendly role claim: the api-gateway
                // (helen.com.gatewayserver) reads a single "role" string claim
                // ("USER"/"ADMIN", without the "ROLE_" prefix) to enforce
                // AuthorizationFilter.hasRole(...) - it does not understand the
                // "roles" list above.
                .add("role", primaryRole(user))
                .build();
    }

    private String primaryRole(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName())
                .min(Comparator.comparingInt(this::priority))
                .map(roleType -> roleType.name().replaceFirst("^ROLE_", ""))
                .orElse("USER");
    }

    private int priority(RoleType roleType) {
        return switch (roleType) {
            case ROLE_ADMIN -> 0;
            case ROLE_MANAGER -> 1;
            case ROLE_USER -> 2;
        };
    }
}
