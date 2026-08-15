package helen.com.authservice.security.mfa;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    void generateSecret_returnsNonEmptyBase32Secret() {
        String secret = totpService.generateSecret();

        assertThat(secret).isNotBlank();
    }

    @Test
    void verifyCode_acceptsCurrentlyValidCode() {
        String secret = totpService.generateSecret();
        int currentCode = new GoogleAuthenticator().getTotpPassword(secret);

        assertThat(totpService.verifyCode(secret, currentCode)).isTrue();
    }

    @Test
    void verifyCode_rejectsObviouslyWrongCode() {
        String secret = totpService.generateSecret();

        assertThat(totpService.verifyCode(secret, 0)).isFalse();
    }
}
