package co.unillanos.secct.usecases.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultadoClasificacionTest {

    
    
    

    @Test
    void shouldCreateResultadoWithCorrectData() {
        ResultadoClasificacion resultado = new ResultadoClasificacion(3, 0.87);

        assertEquals(3, resultado.getCategoriaNtc());
        assertEquals(0.87, resultado.getPuntajeConfianza(), 1e-9);
    }

    @Test
    void shouldCreateResultadoWhenCategoriaNtcIsAtLowerBoundary() {
        ResultadoClasificacion resultado = new ResultadoClasificacion(1, 0.5);
        assertEquals(1, resultado.getCategoriaNtc());
    }

    @Test
    void shouldCreateResultadoWhenCategoriaNtcIsAtUpperBoundary() {
        ResultadoClasificacion resultado = new ResultadoClasificacion(5, 0.5);
        assertEquals(5, resultado.getCategoriaNtc());
    }

    @Test
    void shouldCreateResultadoWhenPuntajeIsZero() {
        ResultadoClasificacion resultado = new ResultadoClasificacion(2, 0.0);
        assertEquals(0.0, resultado.getPuntajeConfianza(), 1e-9);
    }

    @Test
    void shouldCreateResultadoWhenPuntajeIsOne() {
        ResultadoClasificacion resultado = new ResultadoClasificacion(4, 1.0);
        assertEquals(1.0, resultado.getPuntajeConfianza(), 1e-9);
    }

    
    
    

    @Test
    void shouldThrowWhenCategoriaNtcIsBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoClasificacion(0, 0.5));
    }

    @Test
    void shouldThrowWhenCategoriaNtcIsAboveFive() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoClasificacion(6, 0.5));
    }

    @Test
    void shouldThrowWhenCategoriaNtcIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoClasificacion(-1, 0.5));
    }

    
    
    

    @Test
    void shouldThrowWhenPuntajeConfianzaIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoClasificacion(3, -0.01));
    }

    @Test
    void shouldThrowWhenPuntajeConfianzaIsAboveOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoClasificacion(3, 1.01));
    }
}
