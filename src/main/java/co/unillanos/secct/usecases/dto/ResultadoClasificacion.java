package co.unillanos.secct.usecases.dto;


public final class ResultadoClasificacion {

    private final PartePez parte;
    private final int categoriaNtc;
    private final double puntajeConfianza;

    public ResultadoClasificacion(PartePez parte, int categoriaNtc, double puntajeConfianza) {
        this.parte = parte;
        this.categoriaNtc = categoriaNtc;
        this.puntajeConfianza = puntajeConfianza;
    }

    public PartePez getParte() {
        return parte;
    }

    public int getCategoriaNtc() {
        return categoriaNtc;
    }

    public double getPuntajeConfianza() {
        return puntajeConfianza;
    }
}
