package helen.com.authservice.messaging.event;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email
) {
}
