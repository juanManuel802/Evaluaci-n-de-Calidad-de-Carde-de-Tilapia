package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.usecases.ports.GeneradorCodigoLotePort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GeneradorCodigoLoteSecuencial implements GeneradorCodigoLotePort {

    private static final String PREFIJO_DEFAULT = "SECCT";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAXIMO_SECUENCIAL = 999;

    private final String prefijo;
    private int secuencial;

    public GeneradorCodigoLoteSecuencial() {
        this(PREFIJO_DEFAULT);
    }

    public GeneradorCodigoLoteSecuencial(String prefijo) {
        this.prefijo = prefijo;
        this.secuencial = 0;
    }

    @Override
    public CodigoLote generarCodigoLote() {
        if (secuencial >= MAXIMO_SECUENCIAL) {
            throw new IllegalStateException(
                    "El generador ha alcanzado el límite de " + MAXIMO_SECUENCIAL
                            + " códigos para la instancia actual.");
        }
        secuencial++;
        String fecha = LocalDate.now().format(FORMATO_FECHA);
        String seq = String.format("%03d", secuencial);
        return new CodigoLote(prefijo + "-" + fecha + "-" + seq);
    }
}
