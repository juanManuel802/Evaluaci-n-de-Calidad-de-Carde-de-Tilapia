package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.FechaCaptura;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.entities.PesoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLoteRepositoryTest {

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

    private void forzarEstado(Lote lote, EstadoLote estado) throws Exception {
        Field campo = Lote.class.getDeclaredField("estado");
        campo.setAccessible(true);
        campo.set(lote, estado);
    }

    // ------- findById -------

    @Test
    void shouldReturnEmptyWhenLoteNotFound() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();

        Optional<Lote> resultado = repo.findById("INEXISTENTE");

        assertFalse(resultado.isPresent());
    }

    @Test
    void shouldReturnLoteAfterSave() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        Lote l = lote("LOTE-20250524-001", 10);
        repo.save(l);

        Optional<Lote> resultado = repo.findById("LOTE-20250524-001");

        assertTrue(resultado.isPresent());
        assertSame(l, resultado.get());
    }

    @Test
    void shouldUpdateLoteWhenSavedWithSameId() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        Lote original  = lote("LOTE-20250524-002", 5);
        Lote actualizado = lote("LOTE-20250524-002", 10);
        repo.save(original);
        repo.save(actualizado);

        Optional<Lote> resultado = repo.findById("LOTE-20250524-002");
        assertTrue(resultado.isPresent());
        assertSame(actualizado, resultado.get());
        assertEquals(10, resultado.get().getNumeroUnidadesMuestra());
    }

    // ------- constructor con seed -------

    @Test
    void shouldLoadInitialLotes() {
        Lote a = lote("SEED-20250524-001", 3);
        Lote b = lote("SEED-20250524-002", 7);

        InMemoryLoteRepository repo = new InMemoryLoteRepository(
                Arrays.asList(a, b));

        assertTrue(repo.findById("SEED-20250524-001").isPresent());
        assertTrue(repo.findById("SEED-20250524-002").isPresent());
    }

    // ------- existsByCodigo -------

    @Test
    void existsByCodigo_shouldReturnFalseWhenLoteNotFound() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();

        assertFalse(repo.existsByCodigo(new CodigoLote("LOTE-20250524-001")));
    }

    @Test
    void existsByCodigo_shouldReturnTrueAfterSave() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        Lote l = lote("LOTE-20250524-001", 5);
        repo.save(l);

        assertTrue(repo.existsByCodigo(new CodigoLote("LOTE-20250524-001")));
    }

    @Test
    void existsByCodigo_shouldReturnFalseForDifferentCode() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        repo.save(lote("LOTE-20250524-001", 5));

        assertFalse(repo.existsByCodigo(new CodigoLote("LOTE-20250524-002")));
    }

    // ------- findByEstadoIn -------

    @Test
    void findByEstadoIn_shouldReturnEmptyWhenRepositoryIsEmpty() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();

        List<Lote> resultado = repo.findByEstadoIn(EstadoLote.ABIERTO, EstadoLote.EN_EVALUACION);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByEstadoIn_shouldReturnOnlyLotesMatchingState() throws Exception {
        Lote abierto   = lote("LOTA-20250524-001", 5);
        Lote evaluado  = lote("LOTB-20250524-001", 5);
        Lote reportado = lote("LOTC-20250524-001", 5);
        forzarEstado(evaluado, EstadoLote.EVALUADO);
        forzarEstado(reportado, EstadoLote.REPORTADO);

        InMemoryLoteRepository repo = new InMemoryLoteRepository(
                Arrays.asList(abierto, evaluado, reportado));

        List<Lote> resultado = repo.findByEstadoIn(EstadoLote.ABIERTO, EstadoLote.EN_EVALUACION);

        assertEquals(1, resultado.size());
        assertSame(abierto, resultado.get(0));
    }

    @Test
    void findByEstadoIn_shouldIncludeQuotaFullLoteIfStateMatches() {
        // La cuota NO afecta este método; solo filtra por estado
        Lote atQuota = lote("LOTE-20250524-001", 1);
        atQuota.registrarEvaluacion(new Evaluacion("img.jpg", 3, atQuota));
        // atQuota: EN_EVALUACION, 1/1 — cuota completa

        InMemoryLoteRepository repo = new InMemoryLoteRepository(
                Arrays.asList(atQuota));

        List<Lote> resultado = repo.findByEstadoIn(EstadoLote.ABIERTO, EstadoLote.EN_EVALUACION);

        assertEquals(1, resultado.size(),
                "findByEstadoIn solo filtra por estado; la cuota la gestiona el use case.");
    }

    @Test
    void findByEstadoIn_shouldReturnEmptyWhenStateNotInFilter() throws Exception {
        Lote lote = lote("LOTE-20250524-001", 5);
        forzarEstado(lote, EstadoLote.EVALUADO);

        InMemoryLoteRepository repo = new InMemoryLoteRepository(Arrays.asList(lote));

        List<Lote> resultado = repo.findByEstadoIn(EstadoLote.ABIERTO, EstadoLote.EN_EVALUACION);

        assertTrue(resultado.isEmpty());
    }
}
