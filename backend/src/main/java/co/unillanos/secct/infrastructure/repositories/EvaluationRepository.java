package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {

    long countByBatchId(Integer batchId);

    boolean existsByBatchIdAndImageReference(Integer batchId, String imageReference);

    @Query("select new co.unillanos.secct.infrastructure.repositories.BatchEvaluationCount(e.batch.id, count(e)) "
            + "from Evaluation e where e.batch.id in :batchIds group by e.batch.id")
    List<BatchEvaluationCount> countGroupedByBatchIds(@Param("batchIds") List<Integer> batchIds);

    @Query("select cast(avg(p.ntcCategory) as BigDecimal) from EvaluationPoint p where p.evaluation.batch.id = :batchId")
    BigDecimal averageNtcCategoryByBatchId(@Param("batchId") Integer batchId);
}
