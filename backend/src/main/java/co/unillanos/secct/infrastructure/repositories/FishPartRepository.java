package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.FishPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FishPartRepository extends JpaRepository<FishPart, Short> {

    Optional<FishPart> findByCode(String code);
}
