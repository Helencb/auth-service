package helen.com.authservice.entity;


import helen.com.authservice.enums.MFAType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "mfa_secrets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFASecret {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private MFAType type;

    @Column(nullable = false)
    private String secret;

    private Boolean enabled;

    @Column(length = 4000)
    private String backupCodes;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
