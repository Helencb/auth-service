package helen.com.authservice.security.mfa;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around the Google Authenticator (TOTP) library.
 * Isolated behind its own service so MFAValidator/MFAService don't depend
 * directly on the third-party API.
 */
@Service
public class TotpService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    /**
     * Generates a new random Base32 TOTP secret for a user enrolling in MFA.
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Verifies a 6-digit TOTP code against the given secret.
     */
    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }
}
