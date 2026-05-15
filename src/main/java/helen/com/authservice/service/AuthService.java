package helen.com.authservice.service;

import helen.com.authservice.dto.request.LoginRequest;
import helen.com.authservice.dto.request.RefreshTokenRequest;
import helen.com.authservice.dto.request.RegisterRequest;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder  passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final DeviceTrackingService deviceTrackingService;

    private final JwtService jwtService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .enabled(true)
                .locked(false)
                .build();
        userRepository.save(user);

        Device fakeDevice = Device.builder()
                .deviceName("REGISTER")
                .browser("REGISTER")
                .operatingSystem("REGISTER")
                .ipAddress("0.0.0.0")
                .build();

        return buildLoginResponse(user, fakeDevice);
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        Device device = deviceTrackingService.trackDevice(user, httpRequest);
        return buildLoginResponse(user, device);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.getRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

            User user = storedToken.getUser();

            String accessToken = jwtService.generateAccessToken(user);

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(storedToken.getToken())
                    .build();

    }

    private LoginResponse buildLoginResponse(User user, Device device){
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        sessionService.createSession(
                user,
                device,
                accessToken,
                refreshTokenValue
        );

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .tokens(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshTokenValue)
                        .build())
                .build();
    }
}
