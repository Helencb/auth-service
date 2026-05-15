package helen.com.authservice.repository;

import helen.com.authservice.entity.User;
import helen.com.authservice.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<UserSession, String> {
    List<UserSession> findByUserAndActiveTrue(User user);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    Optional<UserSession> findByAccessToken(String accessToken);
}
