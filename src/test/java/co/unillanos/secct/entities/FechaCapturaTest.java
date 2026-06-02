package co.unillanos.secct.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FechaCapturaTest {

    @Test
    void shouldCreateFechaCapturaWhenDateIsToday() {
        LocalDate hoy = LocalDate.now();
        FechaCaptura fc = new FechaCaptura(hoy);
        assertEquals(hoy, fc.getValor());
    }

    @Test
    void shouldCreateFechaCapturaWhenDateIsInThePast() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        FechaCaptura fc = new FechaCaptura(ayer);
        assertEquals(ayer, fc.getValor());
    }

    @Test
    void shouldEnforceRN_003_WhenDateIsFuture() {
        LocalDate manana = LocalDate.now().plusDays(1);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new FechaCaptura(manana));
        assertTrue(ex.getMessage().contains("RN-003"));
    }

    @Test
    void shouldEnforceRN_003_WhenValueIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new FechaCaptura(null));
        assertTrue(ex.getMessage().contains("RN-003"));
    }

    @Test
    void shouldBeEqualByValueWhenSameDate() {
        LocalDate fecha = LocalDate.of(2025, 5, 24);
        FechaCaptura a = new FechaCaptura(fecha);
        FechaCaptura b = new FechaCaptura(fecha);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentDate() {
        FechaCaptura a = new FechaCaptura(LocalDate.of(2025, 5, 24));
        FechaCaptura b = new FechaCaptura(LocalDate.of(2025, 5, 23));
        assertNotEquals(a, b);
    }

    @Test
    void shouldReturnIsoDateAsToString() {
        LocalDate fecha = LocalDate.of(2025, 5, 24);
        FechaCaptura fc = new FechaCaptura(fecha);
        assertEquals("2025-05-24", fc.toString());
    }
}
