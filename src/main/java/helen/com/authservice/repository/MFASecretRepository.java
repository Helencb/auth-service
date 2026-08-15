package helen.com.authservice.repository;

import helen.com.authservice.entity.MFASecret;
import helen.com.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MFASecretRepository extends JpaRepository<MFASecret, String> {
    Optional<MFASecret> findByUser(User user);
}
