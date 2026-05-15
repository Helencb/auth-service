package helen.com.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest{
        @NotBlank(message = "Email obrigatório")
        private String username;

        @Email
        private String email;

        @NotBlank(message = "Senha obrigatório")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String password;

}
