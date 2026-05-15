package helen.com.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyMFARequest {
    @NotBlank
    private String username;

    @NotBlank
    private String code;
}
