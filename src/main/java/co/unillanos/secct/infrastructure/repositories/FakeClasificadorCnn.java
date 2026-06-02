package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;

public class FakeClasificadorCnn implements ClasificadorCnnPort {

    
    private final int categoriaNtc;

    
    private final double puntajeConfianza;

    
    public FakeClasificadorCnn() {
        this(3, 0.90);
    }

    
    public FakeClasificadorCnn(int categoriaNtc, double puntajeConfianza) {
        this.categoriaNtc = categoriaNtc;
        this.puntajeConfianza = puntajeConfianza;
    }

    
    @Override
    public ResultadoClasificacion clasificar(byte[] imagen) {
        return new ResultadoClasificacion(categoriaNtc, puntajeConfianza);
    }
}
