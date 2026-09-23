package farmaciavenda.service.integracao;

import farmaciavenda.models.ItemVenda;
import java.util.List;

/**
 * Interface cliente para integração externa com o SNGPC (Sistema Nacional de
 * Gerenciamento de Produtos Controlados - ANVISA).
 * Utilizada como dependência para testes com Mock Objects (Mockito).
 */
public interface SngpcClient {

    /**
     * Consulta no webservice da ANVISA a autenticidade e validade da receita médica.
     *
     * @param numeroReceita código identificador da receita médica
     * @param cpfCliente CPF do paciente/comprador
     * @return true se a receita estiver válida e apta para dispensação
     */
    boolean consultarReceita(String numeroReceita, String cpfCliente);

    /**
     * Emite e transmite o registro/notificação de dispensação do medicamento controlado para a ANVISA.
     *
     * @param numeroReceita código identificador da receita médica
     * @param crfFarmaceutico registro no CRF do farmacêutico responsável
     * @param itensControlados lista dos itens controlados vendidos
     * @return número de protocolo emitido pelo webservice do SNGPC
     */
    String emitirNotificacaoSNGPC(String numeroReceita, String crfFarmaceutico, List<ItemVenda> itensControlados);
}
