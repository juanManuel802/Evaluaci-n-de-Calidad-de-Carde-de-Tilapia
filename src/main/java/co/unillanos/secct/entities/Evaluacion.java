package co.unillanos.secct.entities;


public class Evaluacion {

    
    private final String idImagen;

    
    private final int clasificacion;

    
    private final Lote lote;

    
    public Evaluacion(String idImagen, int clasificacion, Lote lote) {
        if (idImagen == null || idImagen.isBlank()) {
            throw new IllegalArgumentException(
                    "idImagen no puede ser nulo ni estar en blanco.");
        }
        if (clasificacion < 1 || clasificacion > 5) {
            throw new IllegalArgumentException(
                    "clasificacion debe estar en el rango [1, 5]; valor recibido: "
                            + clasificacion);
        }
        if (lote == null) {
            throw new IllegalArgumentException(
                    "El lote asociado a la evaluacion no puede ser null (RN-019).");
        }
        this.idImagen = idImagen;
        this.clasificacion = clasificacion;
        this.lote = lote;
    }

    
    public String getIdImagen() {
        return idImagen;
    }

    
    public int getClasificacion() {
        return clasificacion;
    }

    
    public Lote getLote() {
        return lote;
    }
}
