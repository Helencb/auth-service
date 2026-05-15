package helen.com.authservice.security.mfa;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    private final GoogleAuthenticator googleAuthenticator =
            new GoogleAuthenticator();

    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }
    public boolean verifyCode(String secret, Integer code) {
        return googleAuthenticator.authorize(secret, code);
    }
}
