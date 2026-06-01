package co.unillanos.secct.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EvaluacionTest {

    private static final LocalDate FECHA_VALIDA = LocalDate.of(2025, 5, 24);

    private Lote loteValido() {
        return new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Piscicola Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(new BigDecimal("50.00")),
                10,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                "");
    }

    // ------- construcción exitosa -------

    @Test
    void shouldCreateEvaluacionWithCorrectData() {
        Lote lote = loteValido();
        Evaluacion evaluacion = new Evaluacion("img-001.jpg", 3, lote);

        assertEquals("img-001.jpg", evaluacion.getIdImagen());
        assertEquals(3, evaluacion.getClasificacion());
        assertSame(lote, evaluacion.getLote());
    }

    @Test
    void shouldCreateEvaluacionWhenClasificacionIsAtLowerBoundary() {
        Evaluacion evaluacion = new Evaluacion("img-min.jpg", 1, loteValido());
        assertEquals(1, evaluacion.getClasificacion());
    }

    @Test
    void shouldCreateEvaluacionWhenClasificacionIsAtUpperBoundary() {
        Evaluacion evaluacion = new Evaluacion("img-max.jpg", 5, loteValido());
        assertEquals(5, evaluacion.getClasificacion());
    }

    // ------- idImagen -------

    @Test
    void shouldThrowWhenIdImagenIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion(null, 3, loteValido()));
    }

    @Test
    void shouldThrowWhenIdImagenIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion("   ", 3, loteValido()));
    }

    @Test
    void shouldThrowWhenIdImagenIsEmptyString() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion("", 3, loteValido()));
    }

    // ------- clasificacion -------

    @Test
    void shouldThrowWhenClasificacionIsBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion("img-001.jpg", 0, loteValido()));
    }

    @Test
    void shouldThrowWhenClasificacionIsAboveFive() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion("img-001.jpg", 6, loteValido()));
    }

    @Test
    void shouldThrowWhenClasificacionIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion("img-001.jpg", -1, loteValido()));
    }

    // ------- RN-019 -------

    @Test
    void shouldThrowWhenLoteIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluacion("img-001.jpg", 3, null));
    }

    @Test
    void shouldNotExposeMutatorForLote() throws NoSuchMethodException {
        boolean setLoteExists = false;
        for (java.lang.reflect.Method m : Evaluacion.class.getMethods()) {
            if (m.getName().equals("setLote")) {
                setLoteExists = true;
                break;
            }
        }
        assertFalse(setLoteExists,
                "Evaluacion no debe exponer setLote(...) — viola RN-019.");
    }
}
