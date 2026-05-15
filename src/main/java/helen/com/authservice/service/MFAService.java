package helen.com.authservice.service;

import helen.com.authservice.dto.response.MFASetupResponse;
import helen.com.authservice.entity.MFASecret;
import helen.com.authservice.entity.User;
import helen.com.authservice.enums.MFAType;
import helen.com.authservice.repository.MFASecretRepository;
import helen.com.authservice.repository.UserRepository;
import helen.com.authservice.security.mfa.MFAValidator;
import helen.com.authservice.security.mfa.QRCodeGenerator;
import helen.com.authservice.security.mfa.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MFAService {
    private final MFASecretRepository mfaSecretRepository;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final QRCodeGenerator qrCodeGenerator;
    private final MFAValidator mfaValidator;

    public MFASetupResponse setupMFA(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        String secret = totpService.generateSecret();

        String otpAuthUrl =
                "otpauth://totp/AuthService:"
                        + user.getUsername()
                        + "?secret="
                        + secret
                        + "&issuer=AuthService";

        String qrCode = qrCodeGenerator.generateQRCode(otpAuthUrl);

        MFASecret mfaSecret = MFASecret.builder()
                .secret(secret)
                .enabled(false)
                .type(MFAType.TOTP)
                .user(user)
                .build();

        mfaSecretRepository.save(mfaSecret);

        return MFASetupResponse.builder()
                .secret(secret)
                .qrCodeBase64(qrCode)
                .build();
    }

    public void enableMFA(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        MFASecret mfaSecret = mfaSecretRepository.findByUser(user)
                        .orElseThrow();

        boolean valid = mfaValidator.validate(
                        mfaSecret.getSecret(),
                        code);

        if (!valid) {
            throw new RuntimeException("Invalid MFA code");
        }

        mfaSecret.setEnabled(true);
        mfaSecretRepository.save(mfaSecret);
    }

    public boolean verifyCode(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        MFASecret mfaSecret =
                mfaSecretRepository.findByUser(user)
                        .orElseThrow();

        return mfaValidator.validate(mfaSecret.getSecret(), code);
    }

    public boolean isMFAEnabled(User user) {
        return mfaSecretRepository.findByUser(user)
                .map(MFASecret::getEnabled)
                .orElse(false);
    }
}
