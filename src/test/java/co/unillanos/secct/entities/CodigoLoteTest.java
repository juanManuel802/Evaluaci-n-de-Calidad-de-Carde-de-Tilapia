package co.unillanos.secct.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodigoLoteTest {

    @Test
    void shouldCreateCodigoLoteWhenFormatIsValid() {
        CodigoLote codigo = new CodigoLote("ESTMETA-20250524-001");
        assertEquals("ESTMETA-20250524-001", codigo.getValor());
    }

    @Test
    void shouldCreateCodigoLoteWhenPrefixIsNumeric() {
        CodigoLote codigo = new CodigoLote("A1B2-20250101-999");
        assertEquals("A1B2-20250101-999", codigo.getValor());
    }

    @Test
    void shouldEnforceRN_001_WhenFormatIsInvalid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CodigoLote("estmeta-20250524-001"));
        assertTrue(ex.getMessage().contains("RN-001"));
    }

    @Test
    void shouldEnforceRN_001_WhenMissingSequential() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CodigoLote("ESTMETA-20250524"));
        assertTrue(ex.getMessage().contains("RN-001"));
    }

    @Test
    void shouldEnforceRN_001_WhenExceedsMaxLength() {
        String largo = "PREFIJOMUYLARGOEXTRAEXTRA-20250524-001";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CodigoLote(largo));
        assertTrue(ex.getMessage().contains("RN-001"));
    }

    @Test
    void shouldEnforceRN_001_WhenValueIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CodigoLote(null));
        assertTrue(ex.getMessage().contains("RN-001"));
    }

    @Test
    void shouldEnforceRN_001_WhenValueIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CodigoLote("   "));
        assertTrue(ex.getMessage().contains("RN-001"));
    }

    @Test
    void shouldBeEqualByValueWhenSameCode() {
        CodigoLote a = new CodigoLote("ESTMETA-20250524-001");
        CodigoLote b = new CodigoLote("ESTMETA-20250524-001");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentCode() {
        CodigoLote a = new CodigoLote("ESTMETA-20250524-001");
        CodigoLote b = new CodigoLote("ESTMETA-20250524-002");
        assertNotEquals(a, b);
    }

    @Test
    void shouldReturnValueAsToString() {
        CodigoLote codigo = new CodigoLote("ESTMETA-20250524-001");
        assertEquals("ESTMETA-20250524-001", codigo.toString());
    }
}
