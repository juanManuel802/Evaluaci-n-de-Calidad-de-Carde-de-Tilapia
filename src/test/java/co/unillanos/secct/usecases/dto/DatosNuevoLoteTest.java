package co.unillanos.secct.usecases.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DatosNuevoLoteTest {

    @Test
    void shouldStoreAllFieldsWhenConstructed() {
        LocalDate fecha = LocalDate.of(2025, 5, 24);
        BigDecimal peso = new BigDecimal("120.50");

        DatosNuevoLote datos = new DatosNuevoLote(
                "Estacion Piscicola Meta",
                fecha,
                peso,
                15,
                "ESTACION_PISCICOLA",
                "Agua a 18 grados");

        assertEquals("Estacion Piscicola Meta", datos.getEstacionOrigen());
        assertEquals(fecha, datos.getFechaCaptura());
        assertEquals(peso, datos.getPesoTotal());
        assertEquals(15, datos.getNumeroUnidadesMuestra());
        assertEquals("ESTACION_PISCICOLA", datos.getPuntoEvaluacion());
        assertEquals("Agua a 18 grados", datos.getObservaciones());
    }

    @Test
    void shouldStoreNullFieldsWithoutValidation() {
        DatosNuevoLote datos = new DatosNuevoLote(
                null, null, null, 0, null, null);

        assertNull(datos.getEstacionOrigen());
        assertNull(datos.getFechaCaptura());
        assertNull(datos.getPesoTotal());
        assertEquals(0, datos.getNumeroUnidadesMuestra());
        assertNull(datos.getPuntoEvaluacion());
        assertNull(datos.getObservaciones());
    }

    @Test
    void shouldStoreEmptyObservaciones() {
        DatosNuevoLote datos = new DatosNuevoLote(
                "Estacion Meta",
                LocalDate.of(2025, 5, 24),
                new BigDecimal("50.00"),
                5,
                "PLAZA_MERCADO",
                "");

        assertEquals("", datos.getObservaciones());
    }
}
