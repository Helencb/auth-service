package helen.com.authservice.messaging.event;

public record UserCreatedEvent(
        Long userId,
        String email
) {
}
