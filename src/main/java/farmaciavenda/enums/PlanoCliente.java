package farmaciavenda.enums;

/**
 * Enum que define o convênio ou plano associado ao cliente da farmácia.
 */
public enum PlanoCliente {
    SAUDE("Plano de Saúde / Convênio"),
    FIDELIDADE("Programa de Fidelidade"),
    NENHUM("Sem Plano / Particular");

    private final String descricao;

    PlanoCliente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
