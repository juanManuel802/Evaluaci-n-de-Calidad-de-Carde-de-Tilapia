package co.unillanos.secct.usecases.ports;

import co.unillanos.secct.usecases.dto.ResultadoClasificacion;

import java.util.List;

public interface ClasificadorCnnPort {

    List<ResultadoClasificacion> clasificar(byte[] imagen);
}
