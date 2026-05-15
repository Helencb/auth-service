package helen.com.authservice.security.mfa;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MFAValidator {
    private final TotpService totpService;

    public boolean validate(String secret, String code) {
        try {
            return totpService.verifyCode(secret, Integer.parseInt(code));
        } catch (Exception ex) {
            return false;
        }
    }
}
