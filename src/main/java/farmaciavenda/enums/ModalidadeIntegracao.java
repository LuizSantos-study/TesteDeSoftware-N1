package farmaciavenda.enums;

/**
 * Enum que define o tipo/modalidade técnica de processamento do pagamento.
 * TEF (Transferência Eletrônica de Fundos) ou POS (Point of Sale/Maquininha autônoma).
 */
public enum ModalidadeIntegracao {
    TEF("Transferência Eletrônica de Fundos (TEF)"),
    POS("Point of Sale (POS)"),
    NAO_APLICAVEL("Não Aplicável");

    private final String descricao;

    ModalidadeIntegracao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
