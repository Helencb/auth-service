package helen.com.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MFASetupResponse {
    private String secret;
    private String qrCodeBase64;
}
