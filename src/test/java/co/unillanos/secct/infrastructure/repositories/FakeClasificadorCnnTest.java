package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeClasificadorCnnTest {

    
    
    

    @Test
    void shouldReturnDefaultCategoriaThreeAndConfianzaNinety() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn();

        ResultadoClasificacion resultado = fake.clasificar(new byte[0]);

        assertEquals(3, resultado.getCategoriaNtc());
        assertEquals(0.90, resultado.getPuntajeConfianza(), 1e-9);
    }

    
    
    

    @Test
    void shouldReturnConfiguredCategoria() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn(5, 0.75);

        ResultadoClasificacion resultado = fake.clasificar(new byte[0]);

        assertEquals(5, resultado.getCategoriaNtc());
    }

    @Test
    void shouldReturnConfiguredConfianza() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn(1, 0.42);

        ResultadoClasificacion resultado = fake.clasificar(new byte[0]);

        assertEquals(0.42, resultado.getPuntajeConfianza(), 1e-9);
    }

    
    
    

    @Test
    void shouldReturnSameResultRegardlessOfImagePath() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn(2, 0.8);

        ResultadoClasificacion r1 = fake.clasificar(new byte[0]);
        ResultadoClasificacion r2 = fake.clasificar(new byte[]{1});

        assertEquals(r1.getCategoriaNtc(), r2.getCategoriaNtc());
        assertEquals(r1.getPuntajeConfianza(), r2.getPuntajeConfianza(), 1e-9);
    }

}
