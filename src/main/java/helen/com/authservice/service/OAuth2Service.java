package helen.com.authservice.service;

import helen.com.authservice.dto.response.LoginResponse;
import helen.com.authservice.dto.response.TokenResponse;
import helen.com.authservice.entity.Device;
import helen.com.authservice.entity.RefreshToken;
import helen.com.authservice.entity.Role;
import helen.com.authservice.entity.User;
import helen.com.authservice.enums.RoleType;
import helen.com.authservice.repository.RefreshTokenRepository;
import helen.com.authservice.repository.RoleRepository;
import helen.com.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Handles "social login" via OAuth2 providers (currently Google).
 * On first login for a given email, provisions a local User (with a random,
 * unusable password since auth is delegated to the provider); subsequent
 * logins just reuse that account. Issues our own JWT access/refresh tokens
 * so the rest of the API (sessions, MFA, etc.) works the same regardless of
 * how the user authenticated.
 */
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse processOAuthLogin(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalStateException("Provedor OAuth2 " + provider + " não retornou email");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUserFromOAuth(oauth2User, email));

        Device device = Device.builder()
                .deviceName(provider)
                .browser(provider)
                .operatingSystem(provider)
                .ipAddress("oauth2")
                .build();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        sessionService.createSession(user, device, accessToken, refreshTokenValue);

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mfaRequired(false)
                .tokens(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshTokenValue)
                        .build())
                .build();
    }

    private User createUserFromOAuth(OAuth2User oauth2User, String email) {
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USER não encontrada"));

        String name = oauth2User.getAttribute("name");
        String username = (name != null ? name : email.split("@")[0]) + "-" + UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .username(username)
                .email(email)
                // password not used for OAuth2-provisioned accounts - random & never disclosed
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .roles(Set.of(userRole))
                .enabled(true)
                .locked(false)
                .build();

        return userRepository.save(user);
    }
}
