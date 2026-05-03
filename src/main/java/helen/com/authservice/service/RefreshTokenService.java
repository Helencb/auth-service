package helen.com.authservice.service;

import helen.com.authservice.dto.LogoutRequest;
import helen.com.authservice.entity.RefreshToken;
import helen.com.authservice.entity.User;
import helen.com.authservice.repository.RefreshTokenRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRespository repository;
    private final AuthService authService;

    public RefreshToken createRefreshToken(User user){
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiration(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        return repository.save(token);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token inválido"));
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revogado");
        }
        if (refreshToken.getExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token Expirado");
        }
        return refreshToken;
    }

    public void revokeToken(String token) {
        RefreshToken refreshToken =  repository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token não encontrado"));
        refreshToken.setRevoked(true);
        repository.save(refreshToken);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
    }
}
