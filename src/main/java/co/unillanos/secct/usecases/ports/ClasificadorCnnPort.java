package co.unillanos.secct.usecases.ports;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;

import java.nio.file.Path;


public interface ClasificadorCnnPort {

    
    ResultadoClasificacion clasificar(Path imagen);
}
