package co.unillanos.secct.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Audit event persisted by LogService (e.g. BATCH_REGISTERED). */
@Entity
@Table(name = "log")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_evento", nullable = false, length = 50)
    private String eventCode;

    @Column(name = "id_usuario")
    private Integer userId;

    @Column(name = "id_lote")
    private Integer batchId;

    @Column(name = "id_evaluacion")
    private Integer evaluationId;

    @Column(name = "descripcion", length = 500)
    private String description;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LogEntry() {
    }

    public LogEntry(String eventCode, Integer userId, Integer batchId, Integer evaluationId, String description) {
        this.eventCode = eventCode;
        this.userId = userId;
        this.batchId = batchId;
        this.evaluationId = evaluationId;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventCode() {
        return eventCode;
    }
}
