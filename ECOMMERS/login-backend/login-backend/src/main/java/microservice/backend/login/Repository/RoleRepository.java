package microservice.backend.login.Repository;


import java.util.Optional;

import microservice.backend.login.enums.ERole;
import microservice.backend.login.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
