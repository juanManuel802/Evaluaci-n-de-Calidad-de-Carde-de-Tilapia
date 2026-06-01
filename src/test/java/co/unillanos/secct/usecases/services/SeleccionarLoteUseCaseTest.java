package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.FechaCaptura;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.entities.PesoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.LoteRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeleccionarLoteUseCaseTest {

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

    // ------- stubs -------

    private LoteRepository repoVacio() {
        return new LoteRepository() {
            public Optional<Lote> findById(String id) { return Optional.empty(); }
            public List<Lote> findByEstadoIn(EstadoLote... estados) { return Collections.emptyList(); }
            public void save(Lote l) {}
            public boolean existsByCodigo(CodigoLote codigo) { return false; }
        };
    }

    private LoteRepository repoConLote(Lote lote) {
        return new LoteRepository() {
            public Optional<Lote> findById(String id) {
                if (lote.getId().equals(id)) return Optional.of(lote);
                return Optional.empty();
            }
            public List<Lote> findByEstadoIn(EstadoLote... estados) {
                Set<EstadoLote> filtro = new HashSet<>(Arrays.asList(estados));
                return filtro.contains(lote.getEstado())
                        ? Collections.singletonList(lote)
                        : Collections.emptyList();
            }
            public void save(Lote l) {}
            public boolean existsByCodigo(CodigoLote codigo) { return false; }
        };
    }

    private LoteRepository repoConLotes(Lote... lotes) {
        return new LoteRepository() {
            public Optional<Lote> findById(String id) {
                for (Lote l : lotes) {
                    if (l.getId().equals(id)) return Optional.of(l);
                }
                return Optional.empty();
            }
            public List<Lote> findByEstadoIn(EstadoLote... estados) {
                Set<EstadoLote> filtro = new HashSet<>(Arrays.asList(estados));
                List<Lote> resultado = new ArrayList<>();
                for (Lote l : lotes) {
                    if (filtro.contains(l.getEstado())) resultado.add(l);
                }
                return resultado;
            }
            public void save(Lote l) {}
            public boolean existsByCodigo(CodigoLote codigo) { return false; }
        };
    }

    private void forzarEstado(Lote lote, EstadoLote estado) throws Exception {
        Field campo = Lote.class.getDeclaredField("estado");
        campo.setAccessible(true);
        campo.set(lote, estado);
    }

    // ------- execute: flujo principal -------

    @Test
    void shouldReturnFailWhenLoteNotFound() {
        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoVacio());

        OperationResult result = uc.execute("ID-INEXISTENTE");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("ID-INEXISTENTE"));
    }

    @Test
    void shouldReturnOkWhenLoteIsABIERTO() {
        Lote l = lote("LOTE-20250524-003", 10);
        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLote(l));

        OperationResult result = uc.execute("LOTE-20250524-003");

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("LOTE-20250524-003"));
    }

    @Test
    void shouldReturnOkWhenLoteIsEN_EVALUACION() {
        Lote l = lote("LOTE-20250524-004", 10);
        l.registrarEvaluacion(new Evaluacion("img-1.jpg", 2, l));

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLote(l));

        OperationResult result = uc.execute("LOTE-20250524-004");

        assertTrue(result.isSuccess());
        assertEquals(EstadoLote.EN_EVALUACION, l.getEstado());
    }

    // ------- execute: escenarios alternativos -------

    @Test
    void shouldReturnFailWhenLoteIsNotAvailableDueToQuota() {
        Lote l = lote("LOTE-20250524-001", 2);
        l.registrarEvaluacion(new Evaluacion("img-1.jpg", 3, l));
        l.registrarEvaluacion(new Evaluacion("img-2.jpg", 4, l));

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLote(l));

        OperationResult result = uc.execute("LOTE-20250524-001");

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldReturnFailWhenLoteIsEVALUADO() throws Exception {
        Lote l = lote("LOTE-20250524-002", 5);
        forzarEstado(l, EstadoLote.EVALUADO);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLote(l));

        OperationResult result = uc.execute("LOTE-20250524-002");

        assertFalse(result.isSuccess());
    }

    // ------- listarDisponibles -------

    @Test
    void listarDisponibles_shouldReturnEmptyWhenNoLotesAvailable() {
        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoVacio());

        List<Lote> lista = uc.listarDisponibles();

        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    void listarDisponibles_shouldReturnLotesWithStateABIERTOOrEN_EVALUACION() {
        Lote a = lote("LOTA-20250524-001", 5);
        Lote b = lote("LOTB-20250524-001", 8);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLotes(a, b));

        List<Lote> lista = uc.listarDisponibles();

        assertEquals(2, lista.size());
    }

    @Test
    void listarDisponibles_shouldIncludeQuotaFullLoteIfStateIsEN_EVALUACION() {
        Lote atQuota = lote("LOTA-20250524-001", 1);
        atQuota.registrarEvaluacion(new Evaluacion("img.jpg", 3, atQuota));

        Lote normal = lote("LOTB-20250524-001", 5);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLotes(atQuota, normal));

        List<Lote> lista = uc.listarDisponibles();

        assertEquals(2, lista.size(),
                "Debe mostrar ambos lotes; la cuota se verifica al seleccionar, no al listar.");
    }

    @Test
    void listarDisponibles_shouldExcludeLotesWithStateEVALUADO() throws Exception {
        Lote abierto  = lote("LOTA-20250524-001", 5);
        Lote evaluado = lote("LOTB-20250524-001", 5);
        forzarEstado(evaluado, EstadoLote.EVALUADO);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLotes(abierto, evaluado));

        List<Lote> lista = uc.listarDisponibles();

        assertEquals(1, lista.size());
        assertEquals("LOTA-20250524-001", lista.get(0).getId());
    }

    @Test
    void listarDisponibles_shouldExcludeLotesWithStateREPORTADO() throws Exception {
        Lote abierto   = lote("LOTA-20250524-001", 5);
        Lote reportado = lote("LOTB-20250524-001", 5);
        forzarEstado(reportado, EstadoLote.REPORTADO);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLotes(abierto, reportado));

        List<Lote> lista = uc.listarDisponibles();

        assertEquals(1, lista.size());
        assertEquals("LOTA-20250524-001", lista.get(0).getId());
    }

    // ------- listarEvaluados -------

    @Test
    void listarEvaluados_shouldReturnEmptyWhenNoEvaluatedLotes() {
        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoVacio());

        List<Lote> lista = uc.listarEvaluados();

        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    void listarEvaluados_shouldReturnLotesWithStateEVALUADOOrREPORTADO() throws Exception {
        Lote abierto   = lote("LOTA-20250524-001", 5);
        Lote evaluado  = lote("LOTB-20250524-001", 5);
        Lote reportado = lote("LOTC-20250524-001", 5);
        forzarEstado(evaluado, EstadoLote.EVALUADO);
        forzarEstado(reportado, EstadoLote.REPORTADO);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLotes(abierto, evaluado, reportado));

        List<Lote> lista = uc.listarEvaluados();

        assertEquals(2, lista.size());
        assertTrue(lista.stream().anyMatch(l -> l.getId().equals("LOTB-20250524-001")));
        assertTrue(lista.stream().anyMatch(l -> l.getId().equals("LOTC-20250524-001")));
    }

    @Test
    void listarEvaluados_shouldExcludeLotesWithStateABIERTOOrEN_EVALUACION() throws Exception {
        Lote abierto  = lote("LOTA-20250524-001", 5);
        Lote evaluado = lote("LOTB-20250524-001", 5);
        forzarEstado(evaluado, EstadoLote.EVALUADO);

        SeleccionarLoteUseCase uc = new SeleccionarLoteUseCase(repoConLotes(abierto, evaluado));

        List<Lote> lista = uc.listarEvaluados();

        assertEquals(1, lista.size());
        assertEquals("LOTB-20250524-001", lista.get(0).getId());
    }
}
