package helen.com.authservice.service;

import helen.com.authservice.entity.PasswordResetToken;
import helen.com.authservice.entity.User;
import helen.com.authservice.exception.BusinessException;
import helen.com.authservice.repository.PasswordResetTokenRepository;
import helen.com.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("helen")
                .email("helen@example.com")
                .password("old-encoded")
                .build();
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void requestReset_sendsEmailWhenUserExists() {
        when(userRepository.findByEmail("helen@example.com")).thenReturn(Optional.of(user));

        passwordResetService.requestReset("helen@example.com");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("helen@example.com"), contains("/reset-password?token="));
    }

    @Test
    void requestReset_doesNothingWhenUserDoesNotExist() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestReset("nobody@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_updatesPasswordWhenTokenValid() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc")
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass123")).thenReturn("new-encoded");

        passwordResetService.resetPassword("abc", "newPass123");

        assertThat(user.getPassword()).isEqualTo("new-encoded");
        assertThat(token.getUsed()).isTrue();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void resetPassword_throwsWhenTokenExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc")
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("abc", "newPass123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void resetPassword_throwsWhenTokenAlreadyUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc")
                .user(user)
                .used(true)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("abc", "newPass123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("utilizado");
    }

    @Test
    void resetPassword_throwsWhenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("missing", "newPass123"))
                .isInstanceOf(BusinessException.class);
    }

}
