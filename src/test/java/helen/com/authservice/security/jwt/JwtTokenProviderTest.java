package helen.com.authservice.security.jwt;

import helen.com.authservice.config.JwtConfig;
import helen.com.authservice.entity.Role;
import helen.com.authservice.entity.User;
import helen.com.authservice.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtValidator jwtValidator;
    private User user;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig, "secret", "test-secret-key-test-secret-key-123456");
        ReflectionTestUtils.setField(jwtConfig, "expiration", 3600000L);
        ReflectionTestUtils.setField(jwtConfig, "refreshExpiration", 604800000L);

        jwtTokenProvider = new JwtTokenProvider(jwtConfig, new JwtClaimsFactory());
        jwtValidator = new JwtValidator(jwtConfig);

        user = User.builder()
                .id(UUID.randomUUID())
                .username("helen")
                .email("helen@example.com")
                .password("x")
                .roles(Set.of(Role.builder().id(UUID.randomUUID()).name(RoleType.ROLE_USER).build()))
                .build();
    }

    @Test
    void generateAccessToken_isValidAndCarriesUsername() {
        String token = jwtTokenProvider.generateAccessToken(user);

        assertThat(jwtValidator.isValid(token)).isTrue();
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("helen");
    }

    @Test
    void generateRefreshToken_isValidAndCarriesUsername() {
        String token = jwtTokenProvider.generateRefreshToken(user);

        assertThat(jwtValidator.isValid(token)).isTrue();
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("helen");
    }

    @Test
    void generateAccessToken_carriesSingleRoleClaimForGatewayCompatibility() {
        // The api-gateway (helen.com.gatewayserver) reads a single "role" string
        // claim ("USER"/"ADMIN", no "ROLE_" prefix) to enforce
        // AuthorizationFilter.hasRole(...). If this claim's shape ever changes,
        // the gateway's authorization will silently start rejecting everyone.
        String token = jwtTokenProvider.generateAccessToken(user);

        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-key-test-secret-key-123456".getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void isValid_rejectsGarbageToken() {
        assertThat(jwtValidator.isValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void isValid_rejectsTokenSignedWithDifferentSecret() {
        JwtConfig otherConfig = new JwtConfig();
        ReflectionTestUtils.setField(otherConfig, "secret", "a-completely-different-secret-key-abcdef");
        ReflectionTestUtils.setField(otherConfig, "expiration", 3600000L);
        ReflectionTestUtils.setField(otherConfig, "refreshExpiration", 604800000L);
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherConfig, new JwtClaimsFactory());

        String token = otherProvider.generateAccessToken(user);

        assertThat(jwtValidator.isValid(token)).isFalse();
    }
}
