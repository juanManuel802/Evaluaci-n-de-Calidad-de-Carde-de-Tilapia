package co.unillanos.secct.usecases.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    @DisplayName("BusinessException constructors and getters")
    void testBusinessException() {
        BusinessException ex1 = new BusinessException("Error simple");
        assertThat(ex1.getMessage()).isEqualTo("Error simple");
        assertThat(ex1.getHttpStatus()).isEqualTo(400);

        BusinessException ex2 = new BusinessException("Conflicto", 409);
        assertThat(ex2.getMessage()).isEqualTo("Conflicto");
        assertThat(ex2.getHttpStatus()).isEqualTo(409);
    }
}
