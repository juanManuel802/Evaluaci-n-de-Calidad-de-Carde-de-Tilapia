package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.CodigoLote;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorCodigoLoteSecuencialTest {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ------- primer código -------

    @Test
    void shouldGenerateValidCodigoLoteOnFirstCall() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        CodigoLote codigo = gen.generarCodigoLote();
        assertNotNull(codigo);
        assertNotNull(codigo.getValor());
    }

    @Test
    void shouldStartSequentialAtOne() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        CodigoLote codigo = gen.generarCodigoLote();
        assertTrue(codigo.getValor().endsWith("-001"),
                "El primer código debe terminar en -001; obtenido: " + codigo.getValor());
    }

    // ------- contenido del código -------

    @Test
    void shouldUsePrefixInGeneratedCode() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTLLN");
        CodigoLote codigo = gen.generarCodigoLote();
        assertTrue(codigo.getValor().startsWith("ESTLLN-"),
                "El código debe comenzar con el prefijo configurado");
    }

    @Test
    void shouldContainTodaysDateInCode() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        String fechaHoy = LocalDate.now().format(FORMATO);
        CodigoLote codigo = gen.generarCodigoLote();
        assertTrue(codigo.getValor().contains(fechaHoy),
                "El código debe contener la fecha actual " + fechaHoy
                        + "; obtenido: " + codigo.getValor());
    }

    @Test
    void shouldProduceFormatPREFIJO_FECHA_SEQ() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        String fechaHoy = LocalDate.now().format(FORMATO);
        CodigoLote codigo = gen.generarCodigoLote();
        assertEquals("ESTMETA-" + fechaHoy + "-001", codigo.getValor());
    }

    // ------- secuencial incremental -------

    @Test
    void shouldIncrementSequentialOnEachCall() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        gen.generarCodigoLote(); // 001
        gen.generarCodigoLote(); // 002
        CodigoLote tercero = gen.generarCodigoLote(); // 003
        assertTrue(tercero.getValor().endsWith("-003"),
                "El tercer código debe terminar en -003");
    }

    @Test
    void shouldGenerateDistinctCodigosOnMultipleCalls() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        CodigoLote a = gen.generarCodigoLote();
        CodigoLote b = gen.generarCodigoLote();
        assertNotEquals(a, b, "Cada llamada debe producir un CodigoLote distinto");
    }

    @Test
    void shouldProduceValidCodigosForMultipleCalls() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        for (int i = 1; i <= 10; i++) {
            CodigoLote codigo = gen.generarCodigoLote();
            assertNotNull(codigo, "El código #" + i + " no debe ser null");
            assertTrue(codigo.getValor().endsWith(String.format("-%03d", i)),
                    "El código #" + i + " debe terminar en -" + String.format("%03d", i));
        }
    }

    // ------- constructor por defecto -------

    @Test
    void shouldUseDefaultPrefixSECCTWhenNoArgConstructor() {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial();
        CodigoLote codigo = gen.generarCodigoLote();
        assertTrue(codigo.getValor().startsWith("SECCT-"),
                "El prefijo por defecto debe ser 'SECCT'");
    }

    // ------- límite de 999 -------

    @Test
    void shouldThrowWhenSequentialLimitIsExceeded() throws Exception {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        Field campo = GeneradorCodigoLoteSecuencial.class.getDeclaredField("secuencial");
        campo.setAccessible(true);
        campo.set(gen, 999);

        assertThrows(IllegalStateException.class, gen::generarCodigoLote,
                "Debe lanzar IllegalStateException cuando el secuencial supera 999");
    }

    @Test
    void shouldGenerateCodeWhenSequentialIsAtLimit() throws Exception {
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial("ESTMETA");
        Field campo = GeneradorCodigoLoteSecuencial.class.getDeclaredField("secuencial");
        campo.setAccessible(true);
        campo.set(gen, 998);

        CodigoLote codigo = gen.generarCodigoLote();
        assertTrue(codigo.getValor().endsWith("-999"),
                "El último código válido debe terminar en -999");
    }
}
