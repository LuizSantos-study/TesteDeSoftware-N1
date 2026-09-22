package farmaciavenda.models;

import farmaciavenda.enums.FuncaoFuncionario;
import farmaciavenda.enums.StatusVenda;
import farmaciavenda.exception.DescontoInvalidoException;
import farmaciavenda.exception.MedicamentoControladoException;
import farmaciavenda.exception.VendaFinalizadaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa uma Venda na farmácia.
 */
public class Venda {

    private Long id;
    private LocalDateTime dataHora;
    private Cliente cliente;
    private Funcionario funcionario;
    private FormaPagamento formaPagamento;
    private final List<ItemVenda> itens = new ArrayList<>();
    private BigDecimal valorTotal;
    private BigDecimal desconto;
    private StatusVenda status;

    // Atributos de apoio para controle sanitário de medicamentos controlados (SNGPC)
    private String numeroReceita;
    private Funcionario farmaceuticoResponsavel;

    public Venda() {
        this.dataHora = LocalDateTime.now();
        this.status = StatusVenda.PENDENTE;
        this.desconto = BigDecimal.ZERO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public Venda(Long id, Cliente cliente, Funcionario funcionario, FormaPagamento formaPagamento) {
        this();
        this.id = id;
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.formaPagamento = formaPagamento;
    }

    /**
     * Adiciona um item à venda.
     * Bloqueia a adição caso a venda já tenha sido finalizada.
     */
    public void adicionarItem(ItemVenda item) {
        validarVendaNaoFinalizada();

        if (item == null) {
            throw new IllegalArgumentException("O item da venda não pode ser nulo.");
        }
        if (item.getProduto() == null) {
            throw new IllegalArgumentException("O item deve conter um produto válido.");
        }
        if (item.getQtdVendida() <= 0) {
            throw new IllegalArgumentException("A quantidade vendida deve ser superior a zero.");
        }

        item.setVenda(this);
        if (item.getNumeroItem() == null) {
            item.setNumeroItem(this.itens.size() + 1);
        }

        this.itens.add(item);
        calcularValorTotal();
    }

    /**
     * Remove um item da venda.
     * Bloqueia a remoção caso a venda já tenha sido finalizada.
     */
    public boolean removerItem(ItemVenda item) {
        validarVendaNaoFinalizada();

        boolean removido = this.itens.remove(item);
        if (removido) {
            calcularValorTotal();
        }
        return removido;
    }

    /**
     * Calcula o somatório dos subtotais de todos os itens da venda.
     */
    public BigDecimal calcularSubtotalItens() {
        return this.itens.stream()
                .map(ItemVenda::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Aplica um valor de desconto à venda.
     * Impede valores negativos ou descontos que excedam o valor total dos itens.
     */
    public void aplicarDesconto(BigDecimal novoDesconto) {
        validarVendaNaoFinalizada();

        if (novoDesconto == null) {
            this.desconto = BigDecimal.ZERO;
            calcularValorTotal();
            return;
        }

        if (novoDesconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new DescontoInvalidoException("O valor do desconto não pode ser negativo.");
        }

        BigDecimal subtotal = calcularSubtotalItens();
        if (novoDesconto.compareTo(subtotal) > 0) {
            throw new DescontoInvalidoException(
                    "O valor do desconto (R$ " + novoDesconto + ") não pode ser maior que o subtotal dos itens (R$ " + subtotal + ")."
            );
        }

        this.desconto = novoDesconto.setScale(2, RoundingMode.HALF_UP);
        calcularValorTotal();
    }

    /**
     * Recalcula o valor total da venda considerando o subtotal dos itens e o desconto.
     * Garante que o total nunca seja negativo.
     */
    public BigDecimal calcularValorTotal() {
        BigDecimal subtotal = calcularSubtotalItens();
        BigDecimal desc = this.desconto != null ? this.desconto : BigDecimal.ZERO;

        BigDecimal total = subtotal.subtract(desc);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new DescontoInvalidoException("O valor total da venda não pode ser negativo.");
        }

        this.valorTotal = total.setScale(2, RoundingMode.HALF_UP);
        return this.valorTotal;
    }

    /**
     * Verifica se existe algum item controlado (SNGPC) na venda.
     */
    public boolean possuiMedicamentoControlado() {
        return this.itens.stream()
                .map(ItemVenda::getProduto)
                .filter(Objects::nonNull)
                .anyMatch(Produto::isSngpc);
    }

    /**
     * Valida os requisitos sanitários para venda de medicamentos controlados:
     * - Presença do número da receita médica.
     * - Validação por profissional farmacêutico (funcionário da venda ou farmacêutico responsável).
     */
    public void validarMedicamentoControlado() {
        if (possuiMedicamentoControlado()) {
            if (this.numeroReceita == null || this.numeroReceita.trim().isEmpty()) {
                throw new MedicamentoControladoException(
                        "Venda contém medicamento controlado (SNGPC) e exige o número da receita médica."
                );
            }

            boolean funcionarioEhFarmaceutico = (this.funcionario != null &&
                    this.funcionario.getFuncao() == FuncaoFuncionario.FARMACEUTICO);

            boolean farmaceuticoResponsavelValido = (this.farmaceuticoResponsavel != null &&
                    this.farmaceuticoResponsavel.getFuncao() == FuncaoFuncionario.FARMACEUTICO);

            if (!funcionarioEhFarmaceutico && !farmaceuticoResponsavelValido) {
                throw new MedicamentoControladoException(
                        "Venda de medicamento controlado exige a validação/presença de um profissional Farmacêutico."
                );
            }
        }
    }

    /**
     * Finaliza a venda após validar regras de negócio:
     * - Não pode estar finalizada
     * - Não pode estar cancelada
     * - Deve possuir ao menos 1 item
     * - Se houver controlados, valida receita e farmacêutico
     * - Valor total não pode ser negativo
     */
    public void finalizarVenda() {
        validarVendaNaoFinalizada();

        if (this.status == StatusVenda.CANCELADA) {
            throw new IllegalStateException("Não é possível finalizar uma venda que já está cancelada.");
        }

        if (this.itens.isEmpty()) {
            throw new IllegalStateException("Não é possível finalizar uma venda sem itens.");
        }

        validarMedicamentoControlado();
        calcularValorTotal();

        this.status = StatusVenda.FINALIZADA;
    }

    /**
     * Cancela a venda.
     */
    public void cancelarVenda() {
        if (this.status == StatusVenda.CANCELADA) {
            throw new IllegalStateException("A venda já está cancelada.");
        }
        this.status = StatusVenda.CANCELADA;
    }

    /**
     * Regra de bloqueio: impede alterações se a venda já estiver com status FINALIZADA.
     */
    public void validarVendaNaoFinalizada() {
        if (this.status == StatusVenda.FINALIZADA) {
            throw new VendaFinalizadaException("Operação não permitida: a venda já está com status FINALIZADA.");
        }
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        validarVendaNaoFinalizada();
        this.cliente = cliente;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        validarVendaNaoFinalizada();
        this.funcionario = funcionario;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        validarVendaNaoFinalizada();
        this.formaPagamento = formaPagamento;
    }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public BigDecimal getValorTotal() {
        if (this.valorTotal == null) {
            calcularValorTotal();
        }
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        validarVendaNaoFinalizada();
        if (valorTotal != null && valorTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new DescontoInvalidoException("O valor total da venda não pode ser negativo.");
        }
        this.valorTotal = valorTotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public void setStatus(StatusVenda status) {
        this.status = status;
    }

    public String getNumeroReceita() {
        return numeroReceita;
    }

    public void setNumeroReceita(String numeroReceita) {
        validarVendaNaoFinalizada();
        this.numeroReceita = numeroReceita;
    }

    public Funcionario getFarmaceuticoResponsavel() {
        return farmaceuticoResponsavel;
    }

    public void setFarmaceuticoResponsavel(Funcionario farmaceuticoResponsavel) {
        validarVendaNaoFinalizada();
        this.farmaceuticoResponsavel = farmaceuticoResponsavel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venda venda = (Venda) o;
        return Objects.equals(id, venda.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Venda{" +
                "id=" + id +
                ", dataHora=" + dataHora +
                ", cliente=" + (cliente != null ? cliente.getNome() : null) +
                ", funcionario=" + (funcionario != null ? funcionario.getNome() : null) +
                ", formaPagamento=" + (formaPagamento != null ? formaPagamento.getDescricao() : null) +
                ", itens=" + itens.size() +
                ", valorTotal=" + valorTotal +
                ", desconto=" + desconto +
                ", status=" + status +
                '}';
    }
}
