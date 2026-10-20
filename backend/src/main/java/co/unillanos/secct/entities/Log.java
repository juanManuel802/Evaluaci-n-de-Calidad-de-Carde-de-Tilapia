package co.unillanos.secct.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Audit log entry entity according to DC-Log-transversal. */
@Entity
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_evento_id", nullable = false)
    private EventType eventType;

    @Column(name = "usuario_id")
    private Integer userId;

    @Column(name = "lote_id")
    private Integer batchId;

    @Column(name = "evaluacion_id")
    private Integer evaluationId;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "descripcion", length = 500)
    private String description;

    public Log() {
    }

    public Log(EventType eventType, Integer userId, Integer batchId, Integer evaluationId,
               OffsetDateTime occurredAt, String description) {
        this.eventType = eventType;
        this.userId = userId;
        this.batchId = batchId;
        this.evaluationId = evaluationId;
        this.occurredAt = occurredAt;
        this.description = description;
    }

    @PrePersist
    void onPersist() {
        if (this.occurredAt == null) {
            this.occurredAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public Integer getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Integer evaluationId) {
        this.evaluationId = evaluationId;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
