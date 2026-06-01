package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.GeneradorCodigoLotePort;
import co.unillanos.secct.usecases.ports.LoteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarLoteUseCaseTest {

    // ------- constantes de test -------

    private static final String CODIGO_VALIDO   = "ESTMETA-20250524-001";
    private static final LocalDate FECHA_VALIDA  = LocalDate.of(2025, 5, 24);
    private static final BigDecimal PESO_VALIDO  = new BigDecimal("120.50");

    // ------- stubs -------

    private static class CapturingLoteRepository implements LoteRepository {
        private final boolean existeCodigo;
        Lote lotePersistido = null;

        CapturingLoteRepository(boolean existeCodigo) {
            this.existeCodigo = existeCodigo;
        }

        CapturingLoteRepository() { this(false); }

        public Optional<Lote> findById(String id) { return Optional.empty(); }
        public List<Lote> findDisponibles() { return Collections.emptyList(); }
        public List<Lote> findByEstadoIn(EstadoLote... estados) { return Collections.emptyList(); }
        public void save(Lote lote) { this.lotePersistido = lote; }
        public boolean existsByCodigo(CodigoLote codigo) { return existeCodigo; }
    }

    private static GeneradorCodigoLotePort generadorFijo(String codigoStr) {
        return () -> new CodigoLote(codigoStr);
    }

    private static DatosNuevoLote datosValidos() {
        return new DatosNuevoLote(
                CODIGO_VALIDO,
                "Estacion Piscicola Meta",
                FECHA_VALIDA,
                PESO_VALIDO,
                15,
                "ESTACION_PISCICOLA",
                "Agua a 18 grados");
    }

    private static RegistrarLoteUseCase ucConRepoLimpio() {
        return new RegistrarLoteUseCase(
                new CapturingLoteRepository(),
                generadorFijo(CODIGO_VALIDO));
    }

    // ------- flujo principal -------

    @Test
    void shouldReturnOkWhenAllDataIsValid() {
        OperationResult result = ucConRepoLimpio().execute(datosValidos());

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldIncludeCodigoInOkMessage() {
        OperationResult result = ucConRepoLimpio().execute(datosValidos());

        assertTrue(result.getMessage().contains(CODIGO_VALIDO));
    }

    @Test
    void shouldPersistLoteWhenRegistrationSucceeds() {
        CapturingLoteRepository repo = new CapturingLoteRepository();
        RegistrarLoteUseCase uc = new RegistrarLoteUseCase(repo, generadorFijo(CODIGO_VALIDO));

        uc.execute(datosValidos());

        assertNotNull(repo.lotePersistido,
                "El repositorio debe recibir el lote persistido.");
        assertEquals(CODIGO_VALIDO, repo.lotePersistido.getId());
    }

    @Test
    void shouldInitialStateLoteIsABIERTOWhenRegistered() {
        CapturingLoteRepository repo = new CapturingLoteRepository();
        RegistrarLoteUseCase uc = new RegistrarLoteUseCase(repo, generadorFijo(CODIGO_VALIDO));

        uc.execute(datosValidos());

        assertEquals(EstadoLote.ABIERTO, repo.lotePersistido.getEstado());
    }

    @Test
    void shouldNotPersistWhenValidationFails() {
        CapturingLoteRepository repo = new CapturingLoteRepository();
        RegistrarLoteUseCase uc = new RegistrarLoteUseCase(repo, generadorFijo(CODIGO_VALIDO));

        DatosNuevoLote datosInvalidos = new DatosNuevoLote(
                "formato-invalido", // RN-001 violation
                "Estacion Meta", FECHA_VALIDA, PESO_VALIDO, 5, "ESTACION_PISCICOLA", "");

        uc.execute(datosInvalidos);

        assertNull(repo.lotePersistido,
                "No debe persistir cuando la validación falla.");
    }

    // ------- RN-001: formato de código -------

    @Test
    void shouldEnforceRN_001_WhenCodigoHasInvalidFormat() {
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote("codigo-sin-fecha", "Est Meta",
                        FECHA_VALIDA, PESO_VALIDO, 5, "ESTACION_PISCICOLA", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-001"));
    }

    @Test
    void shouldEnforceRN_001_WhenCodigoAlreadyExists() {
        CapturingLoteRepository repo = new CapturingLoteRepository(true); // código ya existe
        RegistrarLoteUseCase uc = new RegistrarLoteUseCase(repo, generadorFijo(CODIGO_VALIDO));

        OperationResult result = uc.execute(datosValidos());

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains(CODIGO_VALIDO));
        assertNull(repo.lotePersistido, "No debe persistir si el código ya existe.");
    }

    // ------- RN-002: estación de origen -------

    @Test
    void shouldEnforceRN_002_WhenEstacionOrigenIsBlank() {
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "   ",
                        FECHA_VALIDA, PESO_VALIDO, 5, "ESTACION_PISCICOLA", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-002"));
    }

    @Test
    void shouldEnforceRN_002_WhenEstacionOrigenExceedsMaxLength() {
        String estacionLarga = "E".repeat(101);
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, estacionLarga,
                        FECHA_VALIDA, PESO_VALIDO, 5, "ESTACION_PISCICOLA", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-002"));
    }

    // ------- RN-003: fecha de captura -------

    @Test
    void shouldEnforceRN_003_WhenFechaCapturaIsFuture() {
        LocalDate manana = LocalDate.now().plusDays(1);
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                        manana, PESO_VALIDO, 5, "ESTACION_PISCICOLA", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-003"));
    }

    // ------- RN-004: peso total -------

    @Test
    void shouldEnforceRN_004_WhenPesoTotalIsBelowMinimum() {
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                        FECHA_VALIDA, new BigDecimal("0.004"), 5, "ESTACION_PISCICOLA", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-004"));
    }

    // ------- RN-005: número de unidades -------

    @Test
    void shouldEnforceRN_005_WhenNumeroUnidadesMuestraIsZero() {
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                        FECHA_VALIDA, PESO_VALIDO, 0, "ESTACION_PISCICOLA", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-005"));
    }

    // ------- RN-006: punto de evaluación -------

    @Test
    void shouldEnforceRN_006_WhenPuntoEvaluacionIsInvalidString() {
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                        FECHA_VALIDA, PESO_VALIDO, 5, "VALOR_INEXISTENTE", ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-006"));
    }

    @Test
    void shouldEnforceRN_006_WhenPuntoEvaluacionIsNull() {
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                        FECHA_VALIDA, PESO_VALIDO, 5, null, ""));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-006"));
    }

    // ------- RN-008: observaciones -------

    @Test
    void shouldEnforceRN_008_WhenObservacionesExceedMaxLength() {
        String largas = "X".repeat(501);
        OperationResult result = ucConRepoLimpio().execute(
                new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                        FECHA_VALIDA, PESO_VALIDO, 5, "ESTACION_PISCICOLA", largas));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("RN-008"));
    }

    // ------- obtenerCodigoNuevoLote -------

    @Test
    void shouldReturnCodigoFromGeneradorWhenObtenerCodigo() {
        CodigoLote esperado = new CodigoLote("ESTMETA-20250524-099");
        RegistrarLoteUseCase uc = new RegistrarLoteUseCase(
                new CapturingLoteRepository(), () -> esperado);

        CodigoLote obtenido = uc.obtenerCodigoNuevoLote();

        assertEquals(esperado, obtenido);
    }

    // ------- todos los puntos de evaluación válidos -------

    @Test
    void shouldAcceptAllValidPuntosDeEvaluacion() {
        String[] puntos = {"ESTACION_PISCICOLA", "CENTRO_ACOPIO",
                           "DISTRIBUIDOR_MAYORISTA", "PLAZA_MERCADO"};

        for (String punto : puntos) {
            CapturingLoteRepository repo = new CapturingLoteRepository();
            RegistrarLoteUseCase uc = new RegistrarLoteUseCase(
                    repo, generadorFijo(CODIGO_VALIDO));

            OperationResult result = uc.execute(
                    new DatosNuevoLote(CODIGO_VALIDO, "Estacion Meta",
                            FECHA_VALIDA, PESO_VALIDO, 5, punto, ""));

            assertTrue(result.isSuccess(),
                    "Punto de evaluación '" + punto + "' debería ser válido.");
        }
    }
}
