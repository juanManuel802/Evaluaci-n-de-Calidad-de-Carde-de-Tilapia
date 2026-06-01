package co.unillanos.secct.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class LoteTest {

    private static final LocalDate FECHA_VALIDA = LocalDate.of(2025, 5, 24);
    private static final BigDecimal PESO_VALIDO = new BigDecimal("50.00");

    private static Lote lote(String codigoStr, int capacidad) {
        return new Lote(
                new CodigoLote(codigoStr),
                "Estacion Piscicola Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                capacidad,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                "");
    }

    private static Lote loteConCapacidad(int capacidad) {
        return lote("TEST-20250524-001", capacidad);
    }

    private static Evaluacion evaluacionPara(Lote lote) {
        return new Evaluacion("img-" + System.nanoTime() + ".jpg", 3, lote);
    }

    private static void forzarEstado(Lote lote, EstadoLote estadoDeseado)
            throws Exception {
        java.lang.reflect.Field campo = Lote.class.getDeclaredField("estado");
        campo.setAccessible(true);
        campo.set(lote, estadoDeseado);
    }

    // ------- construcción exitosa -------

    @Test
    void shouldCreateLoteWithAllFieldsCorrect() {
        CodigoLote codigo = new CodigoLote("ESTMETA-20250524-001");
        FechaCaptura fecha = new FechaCaptura(LocalDate.of(2025, 5, 24));
        PesoLote peso = new PesoLote(new BigDecimal("120.50"));

        Lote lote = new Lote(
                codigo,
                "Estacion Piscicola Meta",
                fecha,
                peso,
                15,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                "Agua a 18 grados");

        assertEquals("ESTMETA-20250524-001", lote.getId());
        assertEquals(codigo, lote.getCodigo());
        assertEquals("Estacion Piscicola Meta", lote.getEstacionOrigen());
        assertEquals(fecha, lote.getFechaCaptura());
        assertEquals(peso, lote.getPesoLote());
        assertEquals(15, lote.getNumeroUnidadesMuestra());
        assertEquals(PuntoEvaluacion.ESTACION_PISCICOLA, lote.getPuntoEvaluacion());
        assertEquals(EstadoLote.ABIERTO, lote.getEstado());
        assertEquals("Agua a 18 grados", lote.getObservaciones());
        assertEquals(0, lote.cantidadEvaluaciones());
    }

    @Test
    void shouldEnforceRN_007_WhenConstructorInitializesStateToABIERTO() {
        Lote lote = loteConCapacidad(5);
        assertEquals(EstadoLote.ABIERTO, lote.getEstado());
    }

    @Test
    void shouldAcceptNullObservacionesAsEmptyString() {
        Lote lote = new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                null);
        assertEquals("", lote.getObservaciones());
    }

    // ------- invariantes de construcción -------

    @Test
    void shouldEnforceRN_001_WhenCodigoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                null,
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_002_WhenEstacionOrigenIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                null,
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_002_WhenEstacionOrigenIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "   ",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_002_WhenEstacionOrigenExceedsMaxLength() {
        String estacionLarga = "E".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                estacionLarga,
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_003_WhenFechaCapturaIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                null,
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_004_WhenPesoLoteIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                null,
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_005_WhenNumeroUnidadesMuestraIsZero() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                0,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_005_WhenNumeroUnidadesMuestraIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                -5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                ""));
    }

    @Test
    void shouldEnforceRN_006_WhenPuntoEvaluacionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                null,
                ""));
    }

    @Test
    void shouldEnforceRN_008_WhenObservacionesExceedMaxLength() {
        String largas = "X".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> new Lote(
                new CodigoLote("TEST-20250524-001"),
                "Estacion Meta",
                new FechaCaptura(FECHA_VALIDA),
                new PesoLote(PESO_VALIDO),
                5,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                largas));
    }

    // ------- RN-012 -------

    @Test
    void shouldEnforceRN_012_WhenLoteIsEVALUADO() throws Exception {
        Lote lote = loteConCapacidad(5);
        forzarEstado(lote, EstadoLote.EVALUADO);
        Evaluacion evaluacion = new Evaluacion("img-001.jpg", 2, lote);
        assertThrows(IllegalStateException.class,
                () -> lote.registrarEvaluacion(evaluacion));
    }

    @Test
    void shouldEnforceRN_012_WhenLoteIsREPORTADO() throws Exception {
        Lote lote = loteConCapacidad(5);
        forzarEstado(lote, EstadoLote.REPORTADO);
        Evaluacion evaluacion = new Evaluacion("img-002.jpg", 4, lote);
        assertThrows(IllegalStateException.class,
                () -> lote.registrarEvaluacion(evaluacion));
    }

    // ------- RN-017 -------

    @Test
    void shouldEnforceRN_017_TransitionABIERTO_to_EN_EVALUACION() {
        Lote lote = loteConCapacidad(5);
        assertEquals(EstadoLote.ABIERTO, lote.getEstado());
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertEquals(EstadoLote.EN_EVALUACION, lote.getEstado());
    }

    @Test
    void shouldNotTransitionAgainOnSecondEvaluacion() {
        Lote lote = loteConCapacidad(5);
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertEquals(EstadoLote.EN_EVALUACION, lote.getEstado());
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertEquals(EstadoLote.EN_EVALUACION, lote.getEstado());
    }

    // ------- cantidadEvaluaciones -------

    @Test
    void cantidadEvaluaciones_shouldReturnCorrectCount() {
        Lote lote = loteConCapacidad(10);
        assertEquals(0, lote.cantidadEvaluaciones());
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertEquals(1, lote.cantidadEvaluaciones());
        lote.registrarEvaluacion(evaluacionPara(lote));
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertEquals(3, lote.cantidadEvaluaciones());
    }

    // ------- estaDisponible -------

    @Test
    void estaDisponible_shouldReturnTrue_whenUnderQuota() {
        Lote lote = loteConCapacidad(5);
        assertTrue(lote.estaDisponible());
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertTrue(lote.estaDisponible());
    }

    @Test
    void estaDisponible_shouldReturnFalse_whenAtQuota() {
        Lote lote = loteConCapacidad(2);
        lote.registrarEvaluacion(evaluacionPara(lote));
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertFalse(lote.estaDisponible());
    }

    @Test
    void estaDisponible_shouldReturnFalse_whenEVALUADO() throws Exception {
        Lote lote = loteConCapacidad(5);
        forzarEstado(lote, EstadoLote.EVALUADO);
        assertFalse(lote.estaDisponible());
    }

    @Test
    void estaDisponible_shouldReturnFalse_whenREPORTADO() throws Exception {
        Lote lote = loteConCapacidad(5);
        forzarEstado(lote, EstadoLote.REPORTADO);
        assertFalse(lote.estaDisponible());
    }

    // ------- colección inmutable -------

    @Test
    void getEvaluaciones_shouldReturnUnmodifiableList() {
        Lote lote = loteConCapacidad(5);
        lote.registrarEvaluacion(evaluacionPara(lote));
        assertThrows(UnsupportedOperationException.class,
                () -> lote.getEvaluaciones().add(evaluacionPara(lote)));
    }

    // ------- null guard -------

    @Test
    void shouldThrowWhenRegistrarEvaluacionReceivesNull() {
        Lote lote = loteConCapacidad(5);
        assertThrows(IllegalArgumentException.class,
                () -> lote.registrarEvaluacion(null));
    }

    // ------- cerrarEvaluacion / RN-020 -------

    @Test
    void shouldEnforceRN_020_TransitionEN_EVALUACION_to_EVALUADO() {
        Lote lote = loteConCapacidad(5);
        lote.registrarEvaluacion(new Evaluacion("img-001.jpg", 3, lote));
        assertEquals(EstadoLote.EN_EVALUACION, lote.getEstado());

        lote.cerrarEvaluacion();

        assertEquals(EstadoLote.EVALUADO, lote.getEstado());
    }

    @Test
    void shouldEnforceRN_020_WhenLoteIsABIERTO_cerrarEvaluacionThrows() {
        Lote lote = loteConCapacidad(5);
        assertEquals(EstadoLote.ABIERTO, lote.getEstado());

        assertThrows(IllegalStateException.class, lote::cerrarEvaluacion);
    }

    @Test
    void shouldEnforceRN_020_WhenLoteIsAlreadyEVALUADO_cerrarEvaluacionThrows() {
        Lote lote = loteConCapacidad(5);
        lote.registrarEvaluacion(new Evaluacion("img-001.jpg", 3, lote));
        lote.cerrarEvaluacion();

        assertThrows(IllegalStateException.class, lote::cerrarEvaluacion);
    }

    // ------- cerrarEvaluacion / RN-021 -------

    @Test
    void shouldEnforceRN_021_WhenNoEvaluaciones_cerrarEvaluacionThrows() throws Exception {
        Lote lote = loteConCapacidad(5);
        forzarEstado(lote, EstadoLote.EN_EVALUACION);

        assertThrows(IllegalStateException.class, lote::cerrarEvaluacion);
    }

    @Test
    void shouldEnforceRN_021_ClasificacionFinalIsArithmeticAverage() {
        Lote lote = loteConCapacidad(5);
        lote.registrarEvaluacion(new Evaluacion("img-001.jpg", 3, lote));
        lote.registrarEvaluacion(new Evaluacion("img-002.jpg", 4, lote));
        lote.registrarEvaluacion(new Evaluacion("img-003.jpg", 2, lote));

        lote.cerrarEvaluacion();

        assertEquals(3.0, lote.getClasificacionFinal(), 0.001);
    }

    @Test
    void shouldEnforceRN_021_ClasificacionFinalWithSingleEvaluacion() {
        Lote lote = loteConCapacidad(5);
        lote.registrarEvaluacion(new Evaluacion("img-001.jpg", 5, lote));

        lote.cerrarEvaluacion();

        assertEquals(5.0, lote.getClasificacionFinal(), 0.001);
    }

    @Test
    void clasificacionFinal_shouldBeZeroBeforeCerrarEvaluacion() {
        Lote lote = loteConCapacidad(5);
        assertEquals(0.0, lote.getClasificacionFinal(), 0.001);
    }
}
