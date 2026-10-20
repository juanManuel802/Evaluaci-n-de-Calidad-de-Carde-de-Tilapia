package co.unillanos.secct.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Result of one fish part inside an evaluation: NTC 1443 category and confidence. */
@Entity
@Table(name = "punto_evaluacion")
public class EvaluationPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false, updatable = false)
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parte_pez_id", nullable = false, updatable = false)
    private FishPart fishPart;

    @Column(name = "categoria_ntc", nullable = false)
    private Short ntcCategory;

    @Column(name = "confianza", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    protected EvaluationPoint() {
    }

    EvaluationPoint(Evaluation evaluation, FishPart fishPart, Short ntcCategory, BigDecimal confidence) {
        this.evaluation = evaluation;
        this.fishPart = fishPart;
        this.ntcCategory = ntcCategory;
        this.confidence = confidence;
    }

    public Integer getId() {
        return id;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public FishPart getFishPart() {
        return fishPart;
    }

    public Short getNtcCategory() {
        return ntcCategory;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }
}
