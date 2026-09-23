package farmaciavenda.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Entidade que representa um item individual pertencente a uma Venda.
 */
public class ItemVenda {

    private Long id;
    private Integer numeroItem;
    private Produto produto;
    private Venda venda;
    private int qtdVendida;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    public ItemVenda() {
        this.qtdVendida = 1;
        this.precoUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public ItemVenda(Produto produto, int qtdVendida) {
        this.produto = produto;
        this.qtdVendida = qtdVendida;
        this.precoUnitario = produto != null ? produto.getPreco() : BigDecimal.ZERO;
        calcularSubtotal();
    }

    public ItemVenda(Produto produto, int qtdVendida, BigDecimal precoUnitario) {
        this.produto = produto;
        this.qtdVendida = qtdVendida;
        this.precoUnitario = precoUnitario;
        calcularSubtotal();
    }

    public ItemVenda(Long id, Integer numeroItem, Produto produto, Venda venda, int qtdVendida, BigDecimal precoUnitario) {
        this.id = id;
        this.numeroItem = numeroItem;
        this.produto = produto;
        this.venda = venda;
        this.qtdVendida = qtdVendida;
        this.precoUnitario = precoUnitario;
        calcularSubtotal();
    }

    /**
     * Calcula automaticamente o subtotal do item: subtotal = precoUnitario * qtdVendida.
     * 
     * @return subtotal calculado com 2 casas decimais.
     */
    public BigDecimal calcularSubtotal() {
        if (this.qtdVendida <= 0) {
            throw new IllegalArgumentException("A quantidade vendida deve ser maior que zero.");
        }
        if (this.precoUnitario == null || this.precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço unitário não pode ser nulo ou negativo.");
        }

        this.subtotal = this.precoUnitario
                .multiply(BigDecimal.valueOf(this.qtdVendida))
                .setScale(2, RoundingMode.HALF_UP);

        return this.subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroItem() {
        return numeroItem;
    }

    public void setNumeroItem(Integer numeroItem) {
        this.numeroItem = numeroItem;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
        if (produto != null && (this.precoUnitario == null || this.precoUnitario.compareTo(BigDecimal.ZERO) == 0)) {
            this.precoUnitario = produto.getPreco();
            calcularSubtotal();
        }
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public int getQtdVendida() {
        return qtdVendida;
    }

    public void setQtdVendida(int qtdVendida) {
        if (qtdVendida <= 0) {
            throw new IllegalArgumentException("A quantidade vendida deve ser maior que zero.");
        }
        this.qtdVendida = qtdVendida;
        if (this.precoUnitario != null) {
            calcularSubtotal();
        }
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        if (precoUnitario != null && precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço unitário não pode ser negativo.");
        }
        this.precoUnitario = precoUnitario != null ? precoUnitario : BigDecimal.ZERO;
        if (this.qtdVendida > 0) {
            calcularSubtotal();
        }
    }

    public BigDecimal getSubtotal() {
        if (this.subtotal == null) {
            calcularSubtotal();
        }
        return subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemVenda itemVenda = (ItemVenda) o;
        return Objects.equals(id, itemVenda.id) &&
                Objects.equals(numeroItem, itemVenda.numeroItem) &&
                Objects.equals(produto, itemVenda.produto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroItem, produto);
    }

    @Override
    public String toString() {
        return "ItemVenda{" +
                "id=" + id +
                ", numeroItem=" + numeroItem +
                ", produto=" + (produto != null ? produto.getNome() : null) +
                ", qtdVendida=" + qtdVendida +
                ", precoUnitario=" + precoUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}
