package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;

import java.nio.file.Path;


public class FakeClasificadorCnn implements ClasificadorCnnPort {

    
    private final int categoriaNtc;

    
    private final double puntajeConfianza;

    
    public FakeClasificadorCnn() {
        this(3, 0.90);
    }

    
    public FakeClasificadorCnn(int categoriaNtc, double puntajeConfianza) {
        
        
        new ResultadoClasificacion(categoriaNtc, puntajeConfianza);
        this.categoriaNtc = categoriaNtc;
        this.puntajeConfianza = puntajeConfianza;
    }

    
    @Override
    public ResultadoClasificacion clasificar(Path imagen) {
        return new ResultadoClasificacion(categoriaNtc, puntajeConfianza);
    }
}
