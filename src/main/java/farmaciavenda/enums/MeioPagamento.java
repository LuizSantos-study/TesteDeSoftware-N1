package farmaciavenda.enums;

/**
 * Enum que define o meio/tipo de pagamento utilizado.
 */
public enum MeioPagamento {
    PIX("PIX"),
    DINHEIRO("Dinheiro"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito");

    private final String descricao;

    MeioPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
