package helen.com.authservice.repository;

import helen.com.authservice.entity.Role;
import helen.com.authservice.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(RoleType name);
}
