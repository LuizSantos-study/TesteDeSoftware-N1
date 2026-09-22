package farmaciavenda.service.integracao;

import farmaciavenda.models.FormaPagamento;
import java.math.BigDecimal;

/**
 * Interface cliente para integração externa com Gateway de Pagamentos (TEF / POS).
 * Utilizada como dependência para testes com Mock Objects (Mockito).
 */
public interface GatewayPagamentoClient {

    /**
     * Simula a autorização de uma transação eletrônica junto à adquirente/operadora.
     *
     * @param formaPagamento dados da forma de pagamento utilizada
     * @param valor valor monetário da transação
     * @return true se autorizado pela adquirente, false caso contrário
     */
    boolean autorizarPagamento(FormaPagamento formaPagamento, BigDecimal valor);

    /**
     * Estorna ou cancela uma transação eletrônica previamente autorizada.
     *
     * @param idTransacao identificador / NSU retornado pela adquirente
     * @param valor valor monetário a ser estornado
     * @return true se o cancelamento for concluído com sucesso
     */
    boolean estornarPagamento(String idTransacao, BigDecimal valor);
}
