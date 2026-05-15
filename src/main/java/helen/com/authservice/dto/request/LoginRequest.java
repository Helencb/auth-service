package helen.com.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
        @NotBlank(message = "Email obrigatório")
        private String username;

        @NotBlank(message = "Senha é obrigatório")
        private String password;
}
