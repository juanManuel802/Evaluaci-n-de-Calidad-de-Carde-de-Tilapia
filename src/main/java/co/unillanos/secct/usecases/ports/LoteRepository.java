package co.unillanos.secct.usecases.ports;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Lote;

import java.util.List;
import java.util.Optional;

public interface LoteRepository {

    Optional<Lote> findById(String id);

    List<Lote> findByEstadoIn(EstadoLote... estados);

    void save(Lote lote);

    boolean existsByCodigo(CodigoLote codigo);
}
