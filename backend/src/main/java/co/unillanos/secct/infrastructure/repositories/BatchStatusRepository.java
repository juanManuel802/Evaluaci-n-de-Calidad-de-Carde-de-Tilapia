package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchStatusRepository extends JpaRepository<BatchStatus, Short> {

    Optional<BatchStatus> findByCode(String code);
}
