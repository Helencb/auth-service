package helen.com.authservice.controller;

import helen.com.authservice.dto.response.LoginResponse;
import helen.com.authservice.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Entry point for social login. The actual redirect/callback dance with the
 * provider (Google) is handled by Spring Security's OAuth2 Login filter
 * (see SecurityConfig); once that completes successfully, Spring Security
 * redirects the browser here with an authenticated OAuth2AuthenticationToken,
 * and we exchange it for our own JWT access/refresh tokens.
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    /**
     * Lists the available "login with ..." URLs the frontend can redirect the
     * user to (Spring Security exposes one authorization endpoint per
     * registered client under /oauth2/authorization/{registrationId}).
     */
    @GetMapping("/providers")
    public Map<String, String> providers() {
        return Map.of("google", "/oauth2/authorization/google");
    }

    /**
     * Called after a successful OAuth2 login (configured as the
     * defaultSuccessUrl in SecurityConfig). Returns our own JWT tokens so the
     * frontend can use the API exactly as it would after a normal /auth/login.
     */
    @GetMapping("/success")
    public ResponseEntity<LoginResponse> success(OAuth2AuthenticationToken authentication) {
        LoginResponse response = oauth2Service.processOAuthLogin(
                authentication.getPrincipal(),
                authentication.getAuthorizedClientRegistrationId()
        );
        return ResponseEntity.ok(response);
    }
}
