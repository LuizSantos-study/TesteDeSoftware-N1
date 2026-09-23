package farmaciavenda.models;

import farmaciavenda.enums.MeioPagamento;
import farmaciavenda.enums.ModalidadeIntegracao;
import java.util.Objects;

/**
 * Entidade que representa a Forma de Pagamento configurada no sistema da farmácia.
 */
public class FormaPagamento {

    private Long id;
    private String descricao;
    private MeioPagamento tipo;
    private ModalidadeIntegracao tipoPagamento;
    private boolean ativo;
    private boolean permiteParcelamento;

    public FormaPagamento() {
        this.ativo = true;
        this.tipoPagamento = ModalidadeIntegracao.NAO_APLICAVEL;
    }

    public FormaPagamento(Long id, String descricao, MeioPagamento tipo,
                          ModalidadeIntegracao tipoPagamento, boolean ativo, boolean permiteParcelamento) {
        this.id = id;
        this.descricao = descricao;
        this.tipo = tipo;
        this.tipoPagamento = tipoPagamento != null ? tipoPagamento : ModalidadeIntegracao.NAO_APLICAVEL;
        this.ativo = ativo;
        this.permiteParcelamento = permiteParcelamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public MeioPagamento getTipo() {
        return tipo;
    }

    public void setTipo(MeioPagamento tipo) {
        this.tipo = tipo;
    }

    public ModalidadeIntegracao getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(ModalidadeIntegracao tipoPagamento) {
        this.tipoPagamento = tipoPagamento != null ? tipoPagamento : ModalidadeIntegracao.NAO_APLICAVEL;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isPermiteParcelamento() {
        return permiteParcelamento;
    }

    public void setPermiteParcelamento(boolean permiteParcelamento) {
        this.permiteParcelamento = permiteParcelamento;
    }

    public boolean isTef() {
        return this.tipoPagamento == ModalidadeIntegracao.TEF;
    }

    public boolean isPos() {
        return this.tipoPagamento == ModalidadeIntegracao.POS;
    }

    public boolean requerIntegracaoEletronica() {
        return isTef() || isPos();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormaPagamento that = (FormaPagamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FormaPagamento{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", tipo=" + tipo +
                ", tipoPagamento=" + tipoPagamento +
                ", ativo=" + ativo +
                ", permiteParcelamento=" + permiteParcelamento +
                '}';
    }
}
