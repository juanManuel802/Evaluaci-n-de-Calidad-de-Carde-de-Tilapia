package co.unillanos.secct.usecases.services;

import co.unillanos.secct.adapters.ui.InicializadorDatos;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.PartePez;
import co.unillanos.secct.infrastructure.repositories.GeneradorCodigoLoteSecuencial;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import co.unillanos.secct.infrastructure.repositories.InMemoryLoteRepository;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecctAppTest {

    private static final BigDecimal PESO = new BigDecimal("50.00");

    private static final ClasificadorCnnPort CNN_STUB =
            imagen -> List.of(new ResultadoClasificacion(PartePez.OJO, 3, 0.90));

    private SecctApp appConRepoVacio() {
        return new SecctApp(
                new InMemoryLoteRepository(),
                CNN_STUB,
                new GeneradorCodigoLoteSecuencial());
    }

    private SecctApp appConSeed() {
        SecctApp app = appConRepoVacio();
        new InicializadorDatos(app).cargar();
        return app;
    }

    // ------- datos iniciales (InicializadorDatos) -------

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
