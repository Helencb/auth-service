package helen.com.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SessionResponse {
    private UUID sessionId;
    private String deviceName;
    private String browser;
    private String operatingSystem;
    private String ipAddress;
    private Boolean active;
    private LocalDateTime createdAt;
}
