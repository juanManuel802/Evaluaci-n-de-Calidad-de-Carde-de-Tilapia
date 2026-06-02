package co.unillanos.secct.entities;

import java.time.LocalDate;
import java.util.Objects;

public final class FechaCaptura {

    private final LocalDate valor;

    public FechaCaptura(LocalDate valor) {
        if (valor == null) {
            throw new IllegalArgumentException(
                    "La fecha de captura no puede ser nula (RN-003).");
        }
        if (valor.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de captura no puede ser posterior a la fecha actual (RN-003): " + valor);
        }
        this.valor = valor;
    }

    public LocalDate getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FechaCaptura)) return false;
        FechaCaptura that = (FechaCaptura) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
