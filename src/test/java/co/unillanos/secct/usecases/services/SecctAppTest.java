package co.unillanos.secct.usecases.services;

import co.unillanos.secct.adapters.ui.InicializadorDatos;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.FechaCaptura;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.entities.PesoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import co.unillanos.secct.infrastructure.repositories.FakeClasificadorCnn;
import co.unillanos.secct.infrastructure.repositories.GeneradorCodigoLoteSecuencial;
import co.unillanos.secct.infrastructure.repositories.InMemoryLoteRepository;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecctAppTest {

    private static final LocalDate FECHA = LocalDate.of(2025, 5, 24);
    private static final BigDecimal PESO = new BigDecimal("50.00");

    private static Lote lote(String codigoStr, int capacidad) {
        return new Lote(
                new CodigoLote(codigoStr),
                "Estacion Piscicola Meta",
                new FechaCaptura(FECHA),
                new PesoLote(PESO),
                capacidad,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                "");
    }

    // ------- helpers -------

    private SecctApp appConRepoVacio() {
        return new SecctApp(
                new InMemoryLoteRepository(),
                new FakeClasificadorCnn(),
                new GeneradorCodigoLoteSecuencial());
    }

    private SecctApp appConLote(Lote l) {
        return new SecctApp(
                new InMemoryLoteRepository(Arrays.asList(l)),
                new FakeClasificadorCnn(),
                new GeneradorCodigoLoteSecuencial());
    }

    private SecctApp appConSeed() {
        SecctApp app = appConRepoVacio();
        new InicializadorDatos(app).cargar();
        return app;
    }

    // ------- datos iniciales -------

    @Test
    void shouldLoadInitialLotesOnDefaultConstruction() {
        SecctApp app = appConSeed();

        List<Lote> disponibles = app.listarLotesDisponibles();

        assertEquals(2, disponibles.size());
    }

    @Test
    void shouldSeedLotesWithNonEmptyIds() {
        SecctApp app = appConSeed();

        app.listarLotesDisponibles().forEach(l -> assertFalse(l.getId().isBlank()));
    }

    @Test
    void shouldStartAllInitialLotesAsABIERTO() {
        SecctApp app = appConSeed();

        app.listarLotesDisponibles().forEach(l ->
                assertEquals(EstadoLote.ABIERTO, l.getEstado()));
    }

    // ------- registrarLote (CU-001) -------

    @Test
    void registrarLote_shouldReturnOkWhenDataIsValid() {
        SecctApp app = appConRepoVacio();

        OperationResult result = app.registrarLote(new DatosNuevoLote(
                "Estacion Piscicola Arauca",
                LocalDate.now(),
                new BigDecimal("75.50"),
                10,
                "CENTRO_ACOPIO",
                ""));

        assertTrue(result.isSuccess());
    }

    @Test
    void registrarLote_shouldPersistLoteAfterRegistration() {
        SecctApp app = appConRepoVacio();

        app.registrarLote(new DatosNuevoLote(
                "Estacion Piscicola Meta",
                LocalDate.now(),
                PESO,
                5,
                "ESTACION_PISCICOLA",
                ""));

        assertEquals(1, app.listarLotesDisponibles().size());
    }

    @Test
    void registrarLote_shouldReturnFailForInvalidData() {
        SecctApp app = appConRepoVacio();

        OperationResult result = app.registrarLote(new DatosNuevoLote(
                "",
                LocalDate.now(),
                PESO,
                5,
                "ESTACION_PISCICOLA",
                ""));

        assertFalse(result.isSuccess());
    }

    // ------- listarLotesDisponibles (CU-002) -------

    @Test
    void listarLotesDisponibles_shouldReturnEmptyWhenNoLotes() {
        SecctApp app = appConRepoVacio();

        assertTrue(app.listarLotesDisponibles().isEmpty());
    }

    @Test
    void listarLotesDisponibles_shouldExcludeQuotaFullLoteBecauseStateIsEVALUADO() {
        // Al completar la cuota, el estado pasa a EVALUADO → no aparece en disponibles
        Lote disponible = lote("LOTA-20250524-001", 5);
        Lote agotado    = lote("LOTB-20250524-001", 1);
        agotado.registrarEvaluacion(new Evaluacion("img.jpg", 3, agotado));

        InMemoryLoteRepository repo = new InMemoryLoteRepository(
                Arrays.asList(disponible, agotado));
        SecctApp app = new SecctApp(repo, new FakeClasificadorCnn(),
                new GeneradorCodigoLoteSecuencial());

        List<Lote> lista = app.listarLotesDisponibles();

        assertEquals(1, lista.size(),
                "El lote con cuota completa es EVALUADO y no aparece en disponibles.");
    }

    // ------- seleccionarLote (CU-002) -------

    @Test
    void seleccionarLote_shouldReturnOkForAvailableLote() {
        Lote l = lote("LOTE-20250524-001", 10);
        SecctApp app = appConLote(l);

        OperationResult result = app.seleccionarLote("LOTE-20250524-001");

        assertTrue(result.isSuccess());
    }

    @Test
    void seleccionarLote_shouldReturnFailForNonExistentLote() {
        SecctApp app = appConRepoVacio();

        OperationResult result = app.seleccionarLote("NO-EXISTE");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("NO-EXISTE"));
    }

    @Test
    void seleccionarLote_shouldReturnFailWhenLoteIsAtQuota() {
        Lote l = lote("LOTE-20250524-002", 1);
        l.registrarEvaluacion(new Evaluacion("img.jpg", 2, l));

        SecctApp app = appConLote(l);

        OperationResult result = app.seleccionarLote("LOTE-20250524-002");

        assertFalse(result.isSuccess());
    }

    // ------- evaluarUnidad (CU-003) -------

    @Test
    void evaluarUnidad_shouldReturnOkAndRegisterEvaluation() {
        Lote l = lote("LOTE-20250524-003", 5);
        SecctApp app = appConLote(l);

        OperationResult result = app.evaluarUnidad("LOTE-20250524-003",
                "tilapia_001.jpg", new byte[0]);

        assertTrue(result.isSuccess());
        assertEquals(1, l.cantidadEvaluaciones());
    }

    @Test
    void evaluarUnidad_shouldReturnFailForNonExistentLote() {
        SecctApp app = appConRepoVacio();

        OperationResult result = app.evaluarUnidad("FANTASMA",
                "img.jpg", new byte[0]);

        assertFalse(result.isSuccess());
    }

    // ------- flujos integrados -------

    @Test
    void shouldIntegrateFullCU001Flow() {
        SecctApp app = appConRepoVacio();

        OperationResult registro = app.registrarLote(new DatosNuevoLote(
                "Estacion Piscicola Arauca",
                LocalDate.now(),
                new BigDecimal("60.00"),
                8,
                "ESTACION_PISCICOLA",
                ""));
        assertTrue(registro.isSuccess());

        List<Lote> lista = app.listarLotesDisponibles();
        assertEquals(1, lista.size());
        assertEquals(EstadoLote.ABIERTO, lista.get(0).getEstado());
    }

    @Test
    void shouldIntegrateFullFlowListSelectAndEvaluateMultipleTimes() {
        SecctApp app = appConSeed();

        List<Lote> disponibles = app.listarLotesDisponibles();
        assertEquals(2, disponibles.size());

        String loteId = disponibles.stream()
                .filter(l -> l.getNumeroUnidadesMuestra() == 15)
                .findFirst().orElseThrow().getId();

        OperationResult seleccion = app.seleccionarLote(loteId);
        assertTrue(seleccion.isSuccess());

        assertTrue(app.evaluarUnidad(loteId, "t1.jpg", new byte[0]).isSuccess());
        assertTrue(app.evaluarUnidad(loteId, "t2.jpg", new byte[0]).isSuccess());
        assertTrue(app.evaluarUnidad(loteId, "t3.jpg", new byte[0]).isSuccess());

        Lote lote = app.listarLotesDisponibles().stream()
                .filter(l -> l.getId().equals(loteId))
                .findFirst()
                .orElseThrow();

        assertEquals(EstadoLote.EN_EVALUACION, lote.getEstado());
        assertEquals(3, lote.cantidadEvaluaciones());
        assertEquals(15, lote.getNumeroUnidadesMuestra());
        assertTrue(lote.estaDisponible());
    }

    @Test
    void shouldIntegrateRegistroYEvaluacionEndToEnd() {
        SecctApp app = appConRepoVacio();

        app.registrarLote(new DatosNuevoLote(
                "Estacion Piscicola Meta",
                LocalDate.now(),
                new BigDecimal("80.00"),
                3,
                "PLAZA_MERCADO",
                ""));
        String loteId = app.listarLotesDisponibles().get(0).getId();

        OperationResult seleccion = app.seleccionarLote(loteId);
        assertTrue(seleccion.isSuccess());

        OperationResult eval1 = app.evaluarUnidad(loteId, "pez1.jpg", new byte[0]);
        OperationResult eval2 = app.evaluarUnidad(loteId, "pez2.jpg", new byte[0]);
        assertTrue(eval1.isSuccess());
        assertTrue(eval2.isSuccess());

        Lote lote = app.listarLotesDisponibles().stream()
                .filter(l -> l.getId().equals(loteId))
                .findFirst()
                .orElseThrow();
        assertEquals(EstadoLote.EN_EVALUACION, lote.getEstado());
        assertEquals(2, lote.cantidadEvaluaciones());
    }

    // ------- evaluarLote (CU-004) -------

    @Test
    void evaluarLote_shouldReturnOkAndTransitionToREPORTADO() {
        Lote l = lote("LOTE-20250524-010", 1);
        l.registrarEvaluacion(new Evaluacion("img.jpg", 3, l));
        SecctApp app = appConLote(l);

        OperationResult result = app.evaluarLote("LOTE-20250524-010");

        assertTrue(result.isSuccess());
        assertEquals(EstadoLote.REPORTADO, l.getEstado());
    }

    @Test
    void evaluarLote_shouldReturnFailWhenLoteNotFound() {
        SecctApp app = appConRepoVacio();

        OperationResult result = app.evaluarLote("NO-EXISTE");

        assertFalse(result.isSuccess());
    }

    @Test
    void evaluarLote_shouldReturnFailWhenLoteIsABIERTO() {
        Lote l = lote("LOTE-20250524-011", 5);
        SecctApp app = appConLote(l);

        OperationResult result = app.evaluarLote("LOTE-20250524-011");

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldIntegrateFullCU001ToCU004Flow() {
        SecctApp app = appConRepoVacio();

        app.registrarLote(new DatosNuevoLote(
                "Estacion Piscicola Meta",
                LocalDate.now(),
                new BigDecimal("60.00"),
                3,
                "ESTACION_PISCICOLA",
                ""));
        String loteId = app.listarLotesDisponibles().get(0).getId();

        app.seleccionarLote(loteId);
        app.evaluarUnidad(loteId, "p1.jpg", new byte[0]);
        app.evaluarUnidad(loteId, "p2.jpg", new byte[0]);
        app.evaluarUnidad(loteId, "p3.jpg", new byte[0]);

        OperationResult resultado = app.evaluarLote(loteId);

        assertTrue(resultado.isSuccess());
        assertTrue(resultado.getMessage().contains(loteId));

        Lote lote = app.listarLotesEvaluados().stream()
                .filter(l -> l.getId().equals(loteId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Lote evaluado no encontrado en el repositorio."));
        assertEquals(EstadoLote.REPORTADO, lote.getEstado());
        assertTrue(lote.getClasificacionFinal() > 0.0);
    }
}
