package co.unillanos.secct.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PesoLoteTest {

    @Test
    void shouldCreatePesoLoteWhenValueIsMinimum() {
        PesoLote peso = new PesoLote(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("0.01"), peso.getValor());
    }

    @Test
    void shouldCreatePesoLoteWhenValueIsAboveMinimum() {
        PesoLote peso = new PesoLote(new BigDecimal("120.50"));
        assertEquals(new BigDecimal("120.50"), peso.getValor());
    }

    @Test
    void shouldNormalizeScaleToTwoDecimals() {
        PesoLote peso = new PesoLote(new BigDecimal("5"));
        assertEquals(2, peso.getValor().scale());
        assertEquals(new BigDecimal("5.00"), peso.getValor());
    }

    @Test
    void shouldRoundHalfUpWhenMoreThanTwoDecimals() {
        PesoLote peso = new PesoLote(new BigDecimal("3.456"));
        assertEquals(new BigDecimal("3.46"), peso.getValor());
    }

    @Test
    void shouldEnforceRN_004_WhenValueIsBelowMinimum() {
        // 0.004 redondea a 0.00, que es < 0.01 kg
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new PesoLote(new BigDecimal("0.004")));
        assertTrue(ex.getMessage().contains("RN-004"));
    }

    @Test
    void shouldEnforceRN_004_WhenValueIsZero() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new PesoLote(BigDecimal.ZERO));
        assertTrue(ex.getMessage().contains("RN-004"));
    }

    @Test
    void shouldEnforceRN_004_WhenValueIsNegative() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new PesoLote(new BigDecimal("-5.00")));
        assertTrue(ex.getMessage().contains("RN-004"));
    }

    @Test
    void shouldEnforceRN_004_WhenValueIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new PesoLote(null));
        assertTrue(ex.getMessage().contains("RN-004"));
    }

    @Test
    void shouldBeEqualByValueWhenSameWeight() {
        PesoLote a = new PesoLote(new BigDecimal("120.50"));
        PesoLote b = new PesoLote(new BigDecimal("120.50"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldBeEqualByValueWhenSameWeightDifferentScale() {
        PesoLote a = new PesoLote(new BigDecimal("5.0"));
        PesoLote b = new PesoLote(new BigDecimal("5.00"));
        assertEquals(a, b);
    }

    @Test
    void shouldNotBeEqualWhenDifferentWeight() {
        PesoLote a = new PesoLote(new BigDecimal("10.00"));
        PesoLote b = new PesoLote(new BigDecimal("10.01"));
        assertNotEquals(a, b);
    }

    @Test
    void shouldIncludeKgUnitInToString() {
        PesoLote peso = new PesoLote(new BigDecimal("120.50"));
        assertEquals("120.50 kg", peso.toString());
    }
}
