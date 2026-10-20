package co.unillanos.secct.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Evaluation of one tilapia unit (one image) inside a batch. */
@Entity
@Table(name = "evaluacion")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false, updatable = false)
    private Batch batch;

    @Column(name = "id_imagen", nullable = false, updatable = false, length = 100)
    private String imageReference;

    @Column(name = "fecha_evaluacion", nullable = false, updatable = false)
    private OffsetDateTime evaluatedAt;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationPoint> points = new ArrayList<>();

    protected Evaluation() {
    }

    public Evaluation(Batch batch, String imageReference) {
        this.batch = batch;
        this.imageReference = imageReference;
    }

    /** Adds the result of one fish part (OJO or PIEL) to this evaluation. */
    public void addPoint(FishPart fishPart, Short ntcCategory, BigDecimal confidence) {
        points.add(new EvaluationPoint(this, fishPart, ntcCategory, confidence));
    }

    @PrePersist
    void onCreate() {
        this.evaluatedAt = OffsetDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public Batch getBatch() {
        return batch;
    }

    public String getImageReference() {
        return imageReference;
    }

    public OffsetDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public List<EvaluationPoint> getPoints() {
        return points;
    }
}
