package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Integer> {

    /** Counts every batch of the date, including logically deleted ones, so codes are never reused. */
    long countByCaptureDate(LocalDate captureDate);

    Optional<Batch> findByCodeAndOwnerIdAndIsDeletedFalse(String code, Integer ownerId);

    List<Batch> findByOwnerIdAndStatusCodeInAndIsDeletedFalse(Integer ownerId, List<String> statusCodes);

    List<Batch> findByOwnerIdAndStatusCodeAndIsDeletedFalse(Integer ownerId, String statusCode);
}
