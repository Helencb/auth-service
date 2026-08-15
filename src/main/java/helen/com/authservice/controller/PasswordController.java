package helen.com.authservice.controller;

import helen.com.authservice.dto.request.ForgotPasswordRequest;
import helen.com.authservice.dto.request.ResetPasswordRequest;
import helen.com.authservice.dto.response.MessageResponse;
import helen.com.authservice.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<MessageResponse> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse(
                "Se o email existir em nossa base, um link de redefinição foi enviado."));
    }

    @PostMapping("/reset")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Senha redefinida com sucesso."));
    }
}
