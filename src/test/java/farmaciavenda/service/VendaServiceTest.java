package farmaciavenda.service;

import farmaciavenda.enums.FuncaoFuncionario;
import farmaciavenda.enums.MeioPagamento;
import farmaciavenda.enums.ModalidadeIntegracao;
import farmaciavenda.enums.StatusVenda;
import farmaciavenda.enums.TipoProduto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Duas situações da N1: pagamento aprovado e receita recusada pelo SNGPC.
 * Os objetos de domínio são reais; somente as dependências externas são mocks.
 */
@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private GatewayPagamentoClient gatewayPagamentoClient;

    @Mock
    private SngpcClient sngpcClient;

    @InjectMocks
    private VendaService vendaService;

    // Situação 1: testar uma venda sem acessar uma operadora de pagamento real.
    @Test
    void deveFinalizarESalvarVendaQuandoPagamentoForAprovado() {
        // Preparar: venda de R$ 20,00 e resposta positiva da operadora.
        FormaPagamento formaPagamento = new FormaPagamento(
                1L, "Cartão de crédito", MeioPagamento.CARTAO_CREDITO,
                ModalidadeIntegracao.TEF, true, false);
        Venda venda = new Venda(null, null, null, formaPagamento);
        Produto produto = new Produto(
                1L, "Dipirona", "Dipirona sódica", TipoProduto.GENERICO,
                false, new BigDecimal("10.00"));
        venda.adicionarItem(new ItemVenda(produto, 2));

        when(gatewayPagamentoClient.autorizarPagamento(formaPagamento, new BigDecimal("20.00")))
                .thenReturn(true);
        when(vendaRepository.salvar(venda)).thenReturn(venda);

        // Executar: chamar o serviço real.
        Venda resultado = vendaService.processarFinalizacaoVenda(venda);

        // Verificar: venda finalizada, valor correto enviado e venda salva.
        assertSame(venda, resultado);
        assertEquals(StatusVenda.FINALIZADA, resultado.getStatus());
        verify(gatewayPagamentoClient).autorizarPagamento(formaPagamento, new BigDecimal("20.00"));
        verify(vendaRepository).salvar(venda);
        verifyNoInteractions(sngpcClient);
    }

    // Situação 2: testar uma recusa externa e impedir cobrança e gravação.
    @Test
    void deveBloquearVendaQuandoReceitaForRecusada() {
        // Preparar: os dados locais estão completos; a recusa virá do SNGPC.
        Cliente cliente = new Cliente();
        cliente.setCpf("11122233344");
        Funcionario farmaceutico = new Funcionario(
                1L, "Farmacêutica", "33344455566", "CRF-12345", FuncaoFuncionario.FARMACEUTICO);
        FormaPagamento formaPagamento = new FormaPagamento(
                1L, "Cartão de crédito", MeioPagamento.CARTAO_CREDITO,
                ModalidadeIntegracao.TEF, true, false);
        Venda venda = new Venda(null, cliente, farmaceutico, formaPagamento);
        Produto controlado = new Produto(
                2L, "Rivotril", "Clonazepam", TipoProduto.REFERENCIA,
                true, new BigDecimal("30.00"));
        venda.adicionarItem(new ItemVenda(controlado, 1));
        venda.setNumeroReceita("RECEITA-123");

        when(sngpcClient.consultarReceita("RECEITA-123", "11122233344")).thenReturn(false);

        // Executar e verificar: a resposta negativa deve gerar uma exceção.
        assertThrows(MedicamentoControladoException.class,
                () -> vendaService.processarFinalizacaoVenda(venda));

        // Verificar: venda pendente, sem cobrança, gravação ou notificação.
        assertEquals(StatusVenda.PENDENTE, venda.getStatus());
        verify(sngpcClient).consultarReceita("RECEITA-123", "11122233344");
        verifyNoInteractions(gatewayPagamentoClient, vendaRepository);
        verifyNoMoreInteractions(sngpcClient);
    }
}
