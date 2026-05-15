package helen.com.authservice.entity;

import helen.com.authservice.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fingerprint;

    private String deviceName;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    private String operatingSystem;

    private String browser;

    private String ipAddress;

    private LocalDateTime lastLoginAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
