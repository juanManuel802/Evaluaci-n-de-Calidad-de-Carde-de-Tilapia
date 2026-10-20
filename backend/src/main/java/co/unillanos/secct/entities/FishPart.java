package co.unillanos.secct.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Part of the fish that is classified: OJO or PIEL. */
@Entity
@Table(name = "parte_pez")
public class FishPart {

    @Id
    private Short id;

    @Column(name = "codigo", nullable = false, unique = true)
    private String code;

    protected FishPart() {
    }

    public Short getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
