package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.FechaCaptura;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.entities.PesoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import co.unillanos.secct.infrastructure.repositories.InMemoryLoteRepository;
import co.unillanos.secct.usecases.dto.OperationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EvaluarLoteUseCaseTest {

    private static final LocalDate FECHA = LocalDate.of(2025, 5, 24);
    private static final BigDecimal PESO = new BigDecimal("50.00");

    private Lote lote(String codigoStr, int capacidad) {
        return new Lote(
                new CodigoLote(codigoStr),
                "Estacion Piscicola Meta",
                new FechaCaptura(FECHA),
                new PesoLote(PESO),
                capacidad,
                PuntoEvaluacion.ESTACION_PISCICOLA,
                "");
    }

    private Lote loteEnEvaluacion(String codigoStr, int... categorias) {
        Lote l = lote(codigoStr, Math.max(categorias.length, 1));
        for (int i = 0; i < categorias.length; i++) {
            l.registrarEvaluacion(new Evaluacion("img-" + (i + 1) + ".jpg", categorias[i], l));
        }
        return l;
    }

    private EvaluarLoteUseCase ucConLote(Lote l) {
        InMemoryLoteRepository repo = new InMemoryLoteRepository(java.util.Arrays.asList(l));
        return new EvaluarLoteUseCase(repo);
    }

    // ------- flujo normal -------

    @Test
    void shouldReturnOkAndTransitionToEVALUADOWhenLoteEnEvaluacion() {
        Lote l = loteEnEvaluacion("LOTE-20250524-001", 3, 4, 2);
        EvaluarLoteUseCase uc = ucConLote(l);

        OperationResult result = uc.execute("LOTE-20250524-001");

        assertTrue(result.isSuccess());
        assertEquals(EstadoLote.EVALUADO, l.getEstado());
    }

    @Test
    void shouldReturnClasificacionFinalInMessageWhenSuccess() {
        Lote l = loteEnEvaluacion("LOTE-20250524-002", 3, 4, 2);
        EvaluarLoteUseCase uc = ucConLote(l);

        OperationResult result = uc.execute("LOTE-20250524-002");

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("3.00"),
                "El mensaje debe incluir la clasificación final (3.00)");
    }

    @Test
    void shouldCalculateCorrectAverageWhenMultipleEvaluaciones() {
        Lote l = loteEnEvaluacion("LOTE-20250524-003", 3, 4, 3, 2, 4, 3, 3, 4, 3, 3, 2, 3, 4, 3, 3);
        EvaluarLoteUseCase uc = ucConLote(l);

        uc.execute("LOTE-20250524-003");

        assertEquals(3.13, l.getClasificacionFinal(), 0.01);
    }

    @Test
    void shouldPersistEvaluadoStateAfterExecution() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        Lote l = loteEnEvaluacion("LOTE-20250524-004", 3);
        repo.save(l);
        EvaluarLoteUseCase uc = new EvaluarLoteUseCase(repo);

        uc.execute("LOTE-20250524-004");

        Lote persistido = repo.findById("LOTE-20250524-004").orElseThrow();
        assertEquals(EstadoLote.EVALUADO, persistido.getEstado());
        assertTrue(persistido.getClasificacionFinal() > 0.0);
    }

    // ------- escenario alternativo A: sin evaluaciones -------

    @Test
    void shouldReturnFailWhenLoteHasNoEvaluaciones() throws Exception {
        Lote l = lote("LOTE-20250524-005", 5);
        java.lang.reflect.Field campo = Lote.class.getDeclaredField("estado");
        campo.setAccessible(true);
        campo.set(l, EstadoLote.EN_EVALUACION);
        EvaluarLoteUseCase uc = ucConLote(l);

        OperationResult result = uc.execute("LOTE-20250524-005");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("al menos una unidad evaluada"));
    }

    // ------- escenario alternativo B: estado no evaluable -------

    @Test
    void shouldReturnFailWhenLoteIsABIERTO() {
        Lote l = lote("LOTE-20250524-006", 5);
        EvaluarLoteUseCase uc = ucConLote(l);

        OperationResult result = uc.execute("LOTE-20250524-006");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("estado de evaluación"));
    }

    @Test
    void shouldReturnFailWhenLoteIsAlreadyEVALUADO() {
        Lote l = loteEnEvaluacion("LOTE-20250524-007", 4);
        l.cerrarEvaluacion();
        EvaluarLoteUseCase uc = ucConLote(l);

        OperationResult result = uc.execute("LOTE-20250524-007");

        assertFalse(result.isSuccess());
    }

    // ------- lote no encontrado -------

    @Test
    void shouldReturnFailWhenLoteNotFound() {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        EvaluarLoteUseCase uc = new EvaluarLoteUseCase(repo);

        OperationResult result = uc.execute("NO-EXISTE");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("NO-EXISTE"));
    }
}
