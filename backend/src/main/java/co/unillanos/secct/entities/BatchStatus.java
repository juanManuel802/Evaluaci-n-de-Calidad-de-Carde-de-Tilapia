package co.unillanos.secct.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Lifecycle status of a batch: ABIERTO, EN_EVALUACION or EVALUADO. */
@Entity
@Table(name = "estado_lote")
public class BatchStatus {

    public static final String OPEN = "ABIERTO";
    public static final String IN_EVALUATION = "EN_EVALUACION";
    public static final String EVALUATED = "EVALUADO";

    @Id
    private Short id;

    @Column(name = "codigo", nullable = false, unique = true)
    private String code;

    public BatchStatus() {
    }

    public BatchStatus(Short id, String code) {
        this.id = id;
        this.code = code;
    }

    public Short getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
