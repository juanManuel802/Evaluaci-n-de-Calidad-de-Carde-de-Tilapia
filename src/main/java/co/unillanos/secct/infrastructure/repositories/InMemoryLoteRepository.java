package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class InMemoryLoteRepository implements LoteRepository {

    private final Map<String, Lote> almacen;

    
    public InMemoryLoteRepository() {
        this.almacen = new LinkedHashMap<String, Lote>();
    }

    
    public InMemoryLoteRepository(List<Lote> lotes) {
        this.almacen = new LinkedHashMap<String, Lote>();
        for (Lote lote : lotes) {
            almacen.put(lote.getId(), lote);
        }
    }

    
    @Override
    public Optional<Lote> findById(String id) {
        return Optional.ofNullable(almacen.get(id));
    }

    
    @Override
    public List<Lote> findByEstadoIn(EstadoLote... estados) {
        Set<EstadoLote> filtro = new HashSet<>(Arrays.asList(estados));
        List<Lote> resultado = new ArrayList<>();
        for (Lote lote : almacen.values()) {
            if (filtro.contains(lote.getEstado())) {
                resultado.add(lote);
            }
        }
        return resultado;
    }

    @Override
    public void save(Lote lote) {
        almacen.put(lote.getId(), lote);
    }

    @Override
    public boolean existsByCodigo(CodigoLote codigo) {
        return almacen.containsKey(codigo.getValor());
    }
}
