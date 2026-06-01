package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FakeClasificadorCnnTest {

    
    
    

    @Test
    void shouldReturnDefaultCategoriaThreeAndConfianzaNinety() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn();

        ResultadoClasificacion resultado = fake.clasificar(Paths.get("img.jpg"));

        assertEquals(3, resultado.getCategoriaNtc());
        assertEquals(0.90, resultado.getPuntajeConfianza(), 1e-9);
    }

    
    
    

    @Test
    void shouldReturnConfiguredCategoria() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn(5, 0.75);

        ResultadoClasificacion resultado = fake.clasificar(Paths.get("img.jpg"));

        assertEquals(5, resultado.getCategoriaNtc());
    }

    @Test
    void shouldReturnConfiguredConfianza() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn(1, 0.42);

        ResultadoClasificacion resultado = fake.clasificar(Paths.get("img.jpg"));

        assertEquals(0.42, resultado.getPuntajeConfianza(), 1e-9);
    }

    
    
    

    @Test
    void shouldReturnSameResultRegardlessOfImagePath() {
        FakeClasificadorCnn fake = new FakeClasificadorCnn(2, 0.8);

        ResultadoClasificacion r1 = fake.clasificar(Paths.get("foto_a.jpg"));
        ResultadoClasificacion r2 = fake.clasificar(Paths.get("ruta", "distinta", "foto_b.png"));

        assertEquals(r1.getCategoriaNtc(), r2.getCategoriaNtc());
        assertEquals(r1.getPuntajeConfianza(), r2.getPuntajeConfianza(), 1e-9);
    }

    
    
    

    @Test
    void shouldThrowWhenCategoriaNtcIsInvalidAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new FakeClasificadorCnn(0, 0.9));
    }

    @Test
    void shouldThrowWhenPuntajeConfianzaIsInvalidAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new FakeClasificadorCnn(3, 1.5));
    }
}
