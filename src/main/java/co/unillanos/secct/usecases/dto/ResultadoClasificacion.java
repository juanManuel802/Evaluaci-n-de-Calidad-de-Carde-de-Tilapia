package co.unillanos.secct.usecases.dto;


public final class ResultadoClasificacion {

    
    private final int categoriaNtc;

    
    private final double puntajeConfianza;

    
    public ResultadoClasificacion(int categoriaNtc, double puntajeConfianza) {
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
