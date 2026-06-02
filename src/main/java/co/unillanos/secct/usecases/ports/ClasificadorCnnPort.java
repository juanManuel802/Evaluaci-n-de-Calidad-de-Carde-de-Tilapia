package co.unillanos.secct.usecases.ports;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;


public interface ClasificadorCnnPort {

    ResultadoClasificacion clasificar(byte[] imagen);
}
