package co.unillanos.secct.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class PesoLote {

    private static final BigDecimal MINIMO = new BigDecimal("0.01");

    private final BigDecimal valor;

    public PesoLote(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException(
                    "El peso del lote no puede ser nulo (RN-004).");
        }
        BigDecimal normalizado = valor.setScale(2, RoundingMode.HALF_UP);
        if (normalizado.compareTo(MINIMO) < 0) {
            throw new IllegalArgumentException(
                    "El peso del lote debe ser mayor o igual a 0.01 kg (RN-004); valor recibido: " + valor);
        }
        this.valor = normalizado;
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PesoLote)) return false;
        PesoLote that = (PesoLote) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor.toPlainString() + " kg";
    }
}
