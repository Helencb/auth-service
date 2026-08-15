package helen.com.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token obrigatório")
    private String token;

    @NotBlank(message = "Nova senha obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String newPassword;
}
