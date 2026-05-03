package helen.com.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha obrigatório")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String password
) {
}
