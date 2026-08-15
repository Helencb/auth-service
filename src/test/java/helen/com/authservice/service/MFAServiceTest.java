package helen.com.authservice.service;

import helen.com.authservice.dto.response.MFASetupResponse;
import helen.com.authservice.entity.MFASecret;
import helen.com.authservice.entity.User;
import helen.com.authservice.exception.UnauthorizedException;
import helen.com.authservice.repository.MFASecretRepository;
import helen.com.authservice.repository.UserRepository;
import helen.com.authservice.security.mfa.MFAValidator;
import helen.com.authservice.security.mfa.QRCodeGenerator;
import helen.com.authservice.security.mfa.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MFAServiceTest {

    @Mock
    private MFASecretRepository mfaSecretRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TotpService totpService;
    @Mock
    private QRCodeGenerator qrCodeGenerator;
    @Mock
    private MFAValidator mfaValidator;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private MFAService mfaService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("helen")
                .email("helen@example.com")
                .password("x")
                .build();
    }

    @Test
    void setupMFA_generatesSecretAndQrCode() {
        when(userRepository.findByUsername("helen")).thenReturn(Optional.of(user));
        when(totpService.generateSecret()).thenReturn("SECRET123");
        when(qrCodeGenerator.generateQRCode(anyString())).thenReturn("base64-qr");

        MFASetupResponse response = mfaService.setupMFA("helen");

        assertThat(response.getSecret()).isEqualTo("SECRET123");
        assertThat(response.getQrCodeBase64()).isEqualTo("base64-qr");
        verify(mfaSecretRepository).save(any(MFASecret.class));
    }

    @Test
    void enableMFA_activatesSecretWhenCodeValid() {
        MFASecret secret = MFASecret.builder().secret("SECRET123").enabled(false).user(user).build();

        when(userRepository.findByUsername("helen")).thenReturn(Optional.of(user));
        when(mfaSecretRepository.findByUser(user)).thenReturn(Optional.of(secret));
        when(mfaValidator.validate("SECRET123", "123456")).thenReturn(true);

        mfaService.enableMFA("helen", "123456");

        assertThat(secret.getEnabled()).isTrue();
        verify(mfaSecretRepository).save(secret);
        verify(emailService).sendMfaEnabledEmail("helen@example.com");
    }

    @Test
    void enableMFA_throwsWhenCodeInvalid() {
        MFASecret secret = MFASecret.builder().secret("SECRET123").enabled(false).user(user).build();

        when(userRepository.findByUsername("helen")).thenReturn(Optional.of(user));
        when(mfaSecretRepository.findByUser(user)).thenReturn(Optional.of(secret));
        when(mfaValidator.validate("SECRET123", "000000")).thenReturn(false);

        assertThatThrownBy(() -> mfaService.enableMFA("helen", "000000"))
                .isInstanceOf(UnauthorizedException.class);

        assertThat(secret.getEnabled()).isFalse();
        verify(mfaSecretRepository, never()).save(secret);
    }

    @Test
    void isMFAEnabled_falseWhenNoSecretConfigured() {
        when(mfaSecretRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThat(mfaService.isMFAEnabled(user)).isFalse();
    }
}
