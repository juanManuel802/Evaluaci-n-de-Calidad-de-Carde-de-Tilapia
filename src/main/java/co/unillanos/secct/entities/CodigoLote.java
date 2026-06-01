package co.unillanos.secct.entities;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CodigoLote {

    private static final Pattern FORMATO = Pattern.compile("[A-Z0-9]+-\\d{8}-\\d{3}");
    private static final int LONGITUD_MAXIMA = 30;

    private final String valor;

    public CodigoLote(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de lote no puede ser nulo ni vacío (RN-001).");
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException(
                    "El código de lote excede los " + LONGITUD_MAXIMA
                            + " caracteres permitidos (RN-001): " + valor);
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException(
                    "El código de lote no cumple el formato [PREFIJO]-[AAAAMMDD]-[NNN] (RN-001): " + valor);
        }
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoLote)) return false;
        CodigoLote that = (CodigoLote) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
