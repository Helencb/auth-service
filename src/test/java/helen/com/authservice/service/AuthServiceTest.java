package helen.com.authservice.service;

import helen.com.authservice.dto.request.LoginRequest;
import helen.com.authservice.dto.request.RefreshTokenRequest;
import helen.com.authservice.dto.request.RegisterRequest;
import helen.com.authservice.dto.response.LoginResponse;
import helen.com.authservice.dto.response.TokenResponse;
import helen.com.authservice.entity.RefreshToken;
import helen.com.authservice.entity.Role;
import helen.com.authservice.entity.User;
import helen.com.authservice.enums.RoleType;
import helen.com.authservice.exception.EmailAlreadyExistsException;
import helen.com.authservice.exception.UnauthorizedException;
import helen.com.authservice.exception.UsernameAlreadyExistsException;
import helen.com.authservice.repository.RefreshTokenRepository;
import helen.com.authservice.repository.RoleRepository;
import helen.com.authservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private MFAService mfaService;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock
    private SessionService sessionService;
    @Mock
    private DeviceTrackingService deviceTrackingService;
    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("helen")
                .email("helen@example.com")
                .password("encoded-password")
                .enabled(true)
                .locked(false)
                .build();
    }

    @Test
    void register_createsUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("helen");
        request.setEmail("helen@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("helen")).thenReturn(false);
        when(userRepository.existsByEmail("helen@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_USER))
                .thenReturn(Optional.of(Role.builder().id(UUID.randomUUID()).name(RoleType.ROLE_USER).build()));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("helen");
        assertThat(response.getTokens().getAccessToken()).isEqualTo("access-token");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(sessionService).createSession(any(), any(), eq("access-token"), eq("refresh-token"));
    }

    @Test
    void register_throwsWhenUsernameTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("helen");
        request.setEmail("helen@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("helen")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void register_throwsWhenEmailTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("helen");
        request.setEmail("helen@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("helen")).thenReturn(false);
        when(userRepository.existsByEmail("helen@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void login_returnsMfaRequiredWhenEnabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("helen");
        request.setPassword("secret123");

        when(userRepository.findByUsername("helen")).thenReturn(Optional.of(user));
        when(mfaService.isMFAEnabled(user)).thenReturn(true);

        LoginResponse response = authService.login(request, httpServletRequest);

        assertThat(response.getMfaRequired()).isTrue();
        assertThat(response.getTokens()).isNull();
        verify(deviceTrackingService, never()).trackDevice(any(), any());
    }

    @Test
    void login_returnsTokensWhenMfaDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("helen");
        request.setPassword("secret123");

        when(userRepository.findByUsername("helen")).thenReturn(Optional.of(user));
        when(mfaService.isMFAEnabled(user)).thenReturn(false);
        when(deviceTrackingService.trackDevice(any(), any()))
                .thenReturn(helen.com.authservice.entity.Device.builder()
                        .deviceName("test").browser("test").operatingSystem("test")
                        .ipAddress("127.0.0.1").build());
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginResponse response = authService.login(request, httpServletRequest);

        assertThat(response.getMfaRequired()).isFalse();
        assertThat(response.getTokens().getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void refresh_throwsWhenTokenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("unknown-token");

        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_throwsWhenTokenRevoked() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("revoked-token");

        RefreshToken storedToken = RefreshToken.builder()
                .token("revoked-token")
                .user(user)
                .revoked(true)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_returnsNewAccessTokenWhenValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-token");

        RefreshToken storedToken = RefreshToken.builder()
                .token("valid-token")
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        TokenResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-token");
    }
}
