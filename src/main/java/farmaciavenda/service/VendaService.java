package farmaciavenda.service;

import farmaciavenda.exception.MedicamentoControladoException;
import farmaciavenda.models.Cliente;
import farmaciavenda.models.FormaPagamento;
import farmaciavenda.models.Funcionario;
import farmaciavenda.models.ItemVenda;
import farmaciavenda.models.Produto;
import farmaciavenda.models.Venda;
import farmaciavenda.repository.VendaRepository;
import farmaciavenda.service.integracao.GatewayPagamentoClient;
import farmaciavenda.service.integracao.SngpcClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Camada de serviço responsável por orquestrar os fluxos de venda,
 * validações de negócio, integração com gateway de pagamento (TEF/POS)
 * e consulta/emissão sanitária junto ao SNGPC (ANVISA).
 */
public class VendaService {

    private final VendaRepository vendaRepository;
    private final GatewayPagamentoClient gatewayPagamentoClient;
    private final SngpcClient sngpcClient;

    public VendaService(VendaRepository vendaRepository,
                        GatewayPagamentoClient gatewayPagamentoClient,
                        SngpcClient sngpcClient) {
        this.vendaRepository = vendaRepository;
        this.gatewayPagamentoClient = gatewayPagamentoClient;
        this.sngpcClient = sngpcClient;
    }

    /**
     * Inicia uma nova venda com dados básicos.
     */
    public Venda iniciarVenda(Cliente cliente, Funcionario funcionario, FormaPagamento formaPagamento) {
        return new Venda(null, cliente, funcionario, formaPagamento);
    }

    /**
     * Adiciona um produto à venda instanciando um ItemVenda com subtotal calculado.
     */
    public ItemVenda adicionarItem(Venda venda, Produto produto, int quantidade) {
        ItemVenda item = new ItemVenda(produto, quantidade);
        venda.adicionarItem(item);
        return item;
    }

    /**
     * Aplica desconto à venda respeitando as regras e limites da entidade.
     */
    public void aplicarDesconto(Venda venda, BigDecimal desconto) {
        venda.aplicarDesconto(desconto);
    }

    /**
     * Processa a finalização completa da venda, executando:
     * 1. Validação de venda não finalizada e presença de itens;
     * 2. Consulta e emissão de notificação no SNGPC (se contiver controlados);
     * 3. Autorização de pagamento via TEF/POS (se modalidade eletrônica);
     * 4. Transição de status da venda para FINALIZADA;
     * 5. Persistência da venda no repositório.
     */
    public Venda processarFinalizacaoVenda(Venda venda) {
        venda.validarVendaNaoFinalizada();

        // 1. Tratamento de medicamentos controlados (SNGPC / ANVISA)
        if (venda.possuiMedicamentoControlado()) {
            venda.validarMedicamentoControlado();

            String cpfCliente = (venda.getCliente() != null) ? venda.getCliente().getCpf() : null;
            boolean receitaValida = sngpcClient.consultarReceita(venda.getNumeroReceita(), cpfCliente);

            if (!receitaValida) {
                throw new MedicamentoControladoException("Receita médica recusada ou inválida no sistema SNGPC.");
            }

            String crfFarmaceutico = (venda.getFarmaceuticoResponsavel() != null && venda.getFarmaceuticoResponsavel().getCrf() != null)
                    ? venda.getFarmaceuticoResponsavel().getCrf()
                    : (venda.getFuncionario() != null ? venda.getFuncionario().getCrf() : "");

            List<ItemVenda> itensControlados = venda.getItens().stream()
                    .filter(item -> item.getProduto() != null && item.getProduto().isSngpc())
                    .collect(Collectors.toList());

            sngpcClient.emitirNotificacaoSNGPC(venda.getNumeroReceita(), crfFarmaceutico, itensControlados);
        }

        // 2. Autorização de pagamento em caso de TEF ou POS
        if (venda.getFormaPagamento() != null && venda.getFormaPagamento().requerIntegracaoEletronica()) {
            boolean autorizado = gatewayPagamentoClient.autorizarPagamento(venda.getFormaPagamento(), venda.getValorTotal());
            if (!autorizado) {
                throw new IllegalStateException("Pagamento via " + venda.getFormaPagamento().getTipoPagamento() + " não autorizado pela operadora.");
            }
        }

        // 3. Conclusão da venda e persistência
        venda.finalizarVenda();
        return vendaRepository.salvar(venda);
    }

    public VendaRepository getVendaRepository() {
        return vendaRepository;
    }

    public GatewayPagamentoClient getGatewayPagamentoClient() {
        return gatewayPagamentoClient;
    }

    public SngpcClient getSngpcClient() {
        return sngpcClient;
    }
}
