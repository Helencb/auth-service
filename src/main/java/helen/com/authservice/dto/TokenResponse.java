package helen.com.authservice.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
