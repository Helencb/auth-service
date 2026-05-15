package helen.com.authservice.controller;

import helen.com.authservice.dto.request.LoginRequest;
import helen.com.authservice.dto.request.RefreshTokenRequest;
import helen.com.authservice.dto.request.RegisterRequest;
import helen.com.authservice.dto.response.LoginResponse;
import helen.com.authservice.dto.response.TokenResponse;
import helen.com.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

     private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
