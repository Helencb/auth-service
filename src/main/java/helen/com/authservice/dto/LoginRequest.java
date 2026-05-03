package helen.com.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email obrigatório")
        @Email(message = "Email inválido")
        String email,

            @NotBlank(message = "Senha é obrigatório")
            String password
) {
}
