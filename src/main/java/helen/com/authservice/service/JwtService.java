package helen.com.authservice.service;

import helen.com.authservice.entity.User;
import helen.com.authservice.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider tokenProvider;

    public String generateAccessToken(User user) {
        return tokenProvider.generateAccessToken(user);
    }

    public String generateRefreshToken(User user) {
        return tokenProvider.generateRefreshToken(user);
    }
}
