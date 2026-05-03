package helen.com.authservice.dto;

public record LogoutRequest(
        String refreshToken
) {
}
