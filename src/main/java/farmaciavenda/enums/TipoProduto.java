package farmaciavenda.enums;

/**
 * Enum que categoriza o tipo do produto comercializado pela farmácia.
 */
public enum TipoProduto {
    REFERENCIA("Medicamento de Referência"),
    GENERICO("Medicamento Genérico"),
    SIMILAR("Medicamento Similar"),
    PERFUMARIA("Perfumaria e Cosméticos");

    private final String descricao;

    TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
