package farmaciavenda.repository;

import farmaciavenda.models.Venda;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositório para operações de persistência e consulta de vendas.
 */
public interface VendaRepository {

    Venda salvar(Venda venda);

    Optional<Venda> buscarPorId(Long id);

    List<Venda> listarTodas();

    void deletar(Long id);
}
