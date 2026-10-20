package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Short> {
    Optional<EventType> findByCode(String code);
}
