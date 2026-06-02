package co.unillanos.secct.usecases.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OperationResultTest {

    @Test
    void shouldBeSuccessfulWhenCreatedWithOk() {
        OperationResult result = OperationResult.ok("Operación exitosa.");

        assertTrue(result.isSuccess());
        assertEquals("Operación exitosa.", result.getMessage());
    }

    @Test
    void shouldBeFailedWhenCreatedWithFail() {
        OperationResult result = OperationResult.fail("Lote no encontrado.");

        assertFalse(result.isSuccess());
        assertEquals("Lote no encontrado.", result.getMessage());
    }

    @Test
    void shouldAllowEmptyMessageInOk() {
        OperationResult result = OperationResult.ok("");

        assertTrue(result.isSuccess());
        assertEquals("", result.getMessage());
    }

    @Test
    void shouldAllowEmptyMessageInFail() {
        OperationResult result = OperationResult.fail("");

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldReturnEmptyStringWhenNullMessagePassedToOk() {
        OperationResult result = OperationResult.ok(null);

        assertTrue(result.isSuccess());
        assertEquals("", result.getMessage());
    }

    @Test
    void shouldReturnEmptyStringWhenNullMessagePassedToFail() {
        OperationResult result = OperationResult.fail(null);

        assertFalse(result.isSuccess());
        assertEquals("", result.getMessage());
    }
}
