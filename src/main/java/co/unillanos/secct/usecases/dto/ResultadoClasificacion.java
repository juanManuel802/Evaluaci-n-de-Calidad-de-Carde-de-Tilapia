package co.unillanos.secct.usecases.dto;


public final class ResultadoClasificacion {

    
    private final int categoriaNtc;

    
    private final double puntajeConfianza;

    
    public ResultadoClasificacion(int categoriaNtc, double puntajeConfianza) {
        if (categoriaNtc < 1 || categoriaNtc > 5) {
            throw new IllegalArgumentException(
                    "categoriaNtc debe estar en [1, 5]; valor recibido: " + categoriaNtc);
        }
        if (puntajeConfianza < 0.0 || puntajeConfianza > 1.0) {
            throw new IllegalArgumentException(
                    "puntajeConfianza debe estar en [0.0, 1.0]; valor recibido: "
                            + puntajeConfianza);
        }
        this.categoriaNtc = categoriaNtc;
        this.puntajeConfianza = puntajeConfianza;
    }

    
    public int getCategoriaNtc() {
        return categoriaNtc;
    }

    
    public double getPuntajeConfianza() {
        return puntajeConfianza;
    }
}
