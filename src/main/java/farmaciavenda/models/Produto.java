package farmaciavenda.models;

import farmaciavenda.enums.TipoProduto;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidade que representa um Produto comercializado na farmácia.
 */
public class Produto {

    private Long id;
    private String nome;
    private String principioAtivo;
    private TipoProduto tipo;
    private boolean sngpc;
    private BigDecimal preco;

    public Produto() {
        this.preco = BigDecimal.ZERO;
    }

    public Produto(Long id, String nome, String principioAtivo, TipoProduto tipo, boolean sngpc, BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.principioAtivo = principioAtivo;
        this.tipo = tipo;
        this.sngpc = sngpc;
        setPreco(preco);
    }

    public Produto(Long id, String nome, String principioAtivo, TipoProduto tipo, boolean sngpc, double preco) {
        this(id, nome, principioAtivo, tipo, sngpc, BigDecimal.valueOf(preco));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public TipoProduto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProduto tipo) {
        this.tipo = tipo;
    }

    public boolean isSngpc() {
        return sngpc;
    }

    public void setSngpc(boolean sngpc) {
        this.sngpc = sngpc;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        if (preco != null && preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço do produto não pode ser negativo.");
        }
        this.preco = preco != null ? preco : BigDecimal.ZERO;
    }

    public void setPreco(double preco) {
        setPreco(BigDecimal.valueOf(preco));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", principioAtivo='" + principioAtivo + '\'' +
                ", tipo=" + tipo +
                ", sngpc=" + sngpc +
                ", preco=" + preco +
                '}';
    }
}
