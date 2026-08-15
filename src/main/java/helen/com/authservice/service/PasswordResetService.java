package helen.com.authservice.service;

import helen.com.authservice.entity.PasswordResetToken;
import helen.com.authservice.entity.User;
import helen.com.authservice.exception.BusinessException;
import helen.com.authservice.repository.PasswordResetTokenRepository;
import helen.com.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Starts the reset flow: generates a one-time token (valid for 1h) and emails a reset link.
     * Always returns normally even if the email doesn't exist, to avoid leaking which emails
     * are registered.
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido"));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new BusinessException("Token já utilizado");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
