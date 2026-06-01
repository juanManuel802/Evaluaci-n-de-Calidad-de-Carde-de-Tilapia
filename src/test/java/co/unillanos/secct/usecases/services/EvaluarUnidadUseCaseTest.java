package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;

import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.FechaCaptura;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.entities.PesoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import co.unillanos.secct.usecases.ports.LoteRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EvaluarUnidadUseCaseTest {

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
            public List<Lote> findDisponibles() { return Collections.emptyList(); }
            public List<Lote> findByEstadoIn(EstadoLote... estados) { return Collections.emptyList(); }
            public void save(Lote lote) {}
            public boolean existsByCodigo(CodigoLote codigo) { return false; }
        };
    }

    private static class CapturingLoteRepository implements LoteRepository {
        private final Lote loteARetornar;
        Lote loteGuardado = null;

        CapturingLoteRepository(Lote loteARetornar) {
            this.loteARetornar = loteARetornar;
        }

        public Optional<Lote> findById(String id) {
            if (loteARetornar != null && loteARetornar.getId().equals(id)) {
                return Optional.of(loteARetornar);
            }
            return Optional.empty();
        }

        public List<Lote> findDisponibles() { return Collections.emptyList(); }

        public void save(Lote lote) {
            this.loteGuardado = lote;
        }

        public List<Lote> findByEstadoIn(EstadoLote... estados) { return Collections.emptyList(); }
        public boolean existsByCodigo(CodigoLote codigo) { return false; }
    }

    private ClasificadorCnnPort clasificadorFijo(int categoria, double confianza) {
        return imagen -> new ResultadoClasificacion(categoria, confianza);
    }

    private static class CapturingClasificador implements ClasificadorCnnPort {
        Path imagenRecibida = null;

        public ResultadoClasificacion clasificar(Path imagen) {
            this.imagenRecibida = imagen;
            return new ResultadoClasificacion(3, 0.9);
        }
    }

    private void forzarEstado(Lote lote, EstadoLote estado) throws Exception {
        Field campo = Lote.class.getDeclaredField("estado");
        campo.setAccessible(true);
        campo.set(lote, estado);
    }

    // ------- tests -------

    @Test
    void shouldReturnFailWhenLoteNotFound() {
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                repoVacio(), clasificadorFijo(3, 0.8));

        OperationResult result = uc.execute("ID-INEXISTENTE",
                Paths.get("tilapia_001.jpg"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("ID-INEXISTENTE"));
    }

    @Test
    void shouldReturnFailWhenLoteIsNotAvailableDueToQuota() {
        Lote l = lote("LOTE-20250524-001", 1);
        l.registrarEvaluacion(new Evaluacion("img-0.jpg", 2, l));

        CapturingLoteRepository repo = new CapturingLoteRepository(l);
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                repo, clasificadorFijo(3, 0.8));

        OperationResult result = uc.execute("LOTE-20250524-001", Paths.get("nueva.jpg"));

        assertFalse(result.isSuccess());
        assertNull(repo.loteGuardado, "No debe persistir cuando el lote no está disponible.");
    }

    @Test
    void shouldReturnFailWhenLoteIsREPORTADO() throws Exception {
        Lote l = lote("LOTE-20250524-002", 5);
        forzarEstado(l, EstadoLote.REPORTADO);

        CapturingLoteRepository repo = new CapturingLoteRepository(l);
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                repo, clasificadorFijo(3, 0.8));

        OperationResult result = uc.execute("LOTE-20250524-002", Paths.get("img.jpg"));

        assertFalse(result.isSuccess());
        assertNull(repo.loteGuardado);
    }

    @Test
    void shouldReturnOkAndPersistWhenEvaluationIsSuccessful() {
        Lote l = lote("LOTE-20250524-003", 10);
        CapturingLoteRepository repo = new CapturingLoteRepository(l);
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                repo, clasificadorFijo(4, 0.95));

        OperationResult result = uc.execute("LOTE-20250524-003",
                Paths.get("tilapia_001.jpg"));

        assertTrue(result.isSuccess());
        assertSame(l, repo.loteGuardado, "Debe persistir el mismo lote.");
        assertEquals(1, l.cantidadEvaluaciones());
    }

    @Test
    void shouldIncludeCategoriaNtcInOkMessage() {
        Lote l = lote("LOTE-20250524-004", 10);
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                new CapturingLoteRepository(l), clasificadorFijo(2, 0.75));

        OperationResult result = uc.execute("LOTE-20250524-004", Paths.get("img.jpg"));

        assertTrue(result.getMessage().contains("2"));
    }

    @Test
    void shouldInvokeCnnPortWithCorrectImage() {
        Lote l = lote("LOTE-20250524-005", 10);
        CapturingClasificador cnn = new CapturingClasificador();
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                new CapturingLoteRepository(l), cnn);
        Path imagen = Paths.get("fotos", "tilapia_007.jpg");

        uc.execute("LOTE-20250524-005", imagen);

        assertEquals(imagen, cnn.imagenRecibida);
    }

    @Test
    void shouldTransitionLoteToEN_EVALUACIONOnFirstEvaluation() {
        Lote l = lote("LOTE-20250524-006", 5);
        assertEquals(EstadoLote.ABIERTO, l.getEstado());

        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                new CapturingLoteRepository(l), clasificadorFijo(3, 0.8));

        uc.execute("LOTE-20250524-006", Paths.get("img.jpg"));

        assertEquals(EstadoLote.EN_EVALUACION, l.getEstado());
    }

    @Test
    void shouldAccumulateEvaluacionesAcrossMultipleCalls() {
        Lote l = lote("LOTE-20250524-007", 5);
        CapturingLoteRepository repo = new CapturingLoteRepository(l);
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                repo, clasificadorFijo(1, 0.6));

        uc.execute("LOTE-20250524-007", Paths.get("img-1.jpg"));
        uc.execute("LOTE-20250524-007", Paths.get("img-2.jpg"));
        uc.execute("LOTE-20250524-007", Paths.get("img-3.jpg"));

        assertEquals(3, l.cantidadEvaluaciones());
    }

    @Test
    void shouldUseFileNameAsIdImagen() {
        Lote l = lote("LOTE-20250524-008", 5);
        CapturingLoteRepository repo = new CapturingLoteRepository(l);
        EvaluarUnidadUseCase uc = new EvaluarUnidadUseCase(
                repo, clasificadorFijo(5, 1.0));

        uc.execute("LOTE-20250524-008", Paths.get("fotos", "captura_final.jpg"));

        Evaluacion evaluacion = l.getEvaluaciones().get(0);
        assertEquals("captura_final.jpg", evaluacion.getIdImagen());
    }
}
