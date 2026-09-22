package farmaciavenda.enums;

/**
 * Enum que define as funções que um funcionário pode exercer na farmácia.
 */
public enum FuncaoFuncionario {
    OPERADOR_CAIXA("Operador de Caixa"),
    FARMACEUTICO("Farmacêutico"),
    GERENTE("Gerente");

    private final String descricao;

    FuncaoFuncionario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
