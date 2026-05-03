package helen.com.authservice.service;

import helen.com.authservice.dto.*;
import helen.com.authservice.entity.RefreshToken;
import helen.com.authservice.entity.User;
import helen.com.authservice.exception.EmailAlreadyExistsException;
import helen.com.authservice.messaging.event.UserCreatedEvent;
import helen.com.authservice.messaging.event.UserLoggedEvent;
import helen.com.authservice.messaging.producer.AuthProducer;
import helen.com.authservice.repository.UserRepository;
import helen.com.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthProducer producer;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        ));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtService.generateToken(userDetails);

        User user = repository.findByEmail(request.email())
                        .orElseThrow();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        producer.publishUserLogged(
                new UserLoggedEvent(request.email())
        );

        return new TokenResponse(accessToken, refreshToken.getToken());
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = refreshToken.getUser();

        UserDetails  userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateToken(userDetails);

        return new TokenResponse(newAccessToken, refreshToken.getToken());
    }

    public TokenResponse register(RegisterRequest request) {
        if(repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email já cadastrado");
        }

    User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role("ROLE_USER")
            .build();
        repository.save(user);

        producer.publishUserCreated(
                new UserCreatedEvent(
                        user.getId(),
                        user.getEmail())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken.getToken());
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revokeToken(request.refreshToken());
    }
}
