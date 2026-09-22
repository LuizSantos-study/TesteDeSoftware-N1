package farmaciavenda.models;

import farmaciavenda.enums.FuncaoFuncionario;
import java.util.Objects;

/**
 * Entidade que representa um Funcionário da farmácia.
 */
public class Funcionario {

    private Long id;
    private String nome;
    private String cpf;
    private String crf; // Registro no Conselho Regional de Farmácia (obrigatório se Farmacêutico)
    private FuncaoFuncionario funcao;

    public Funcionario() {
    }

    public Funcionario(Long id, String nome, String cpf, FuncaoFuncionario funcao) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.funcao = funcao;
    }

    public Funcionario(Long id, String nome, String cpf, String crf, FuncaoFuncionario funcao) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.crf = crf;
        this.funcao = funcao;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCrf() {
        return crf;
    }

    public void setCrf(String crf) {
        this.crf = crf;
    }

    public FuncaoFuncionario getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoFuncionario funcao) {
        this.funcao = funcao;
    }

    public boolean isFarmaceutico() {
        return this.funcao == FuncaoFuncionario.FARMACEUTICO;
    }

    public boolean isGerente() {
        return this.funcao == FuncaoFuncionario.GERENTE;
    }

    public boolean isOperadorCaixa() {
        return this.funcao == FuncaoFuncionario.OPERADOR_CAIXA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(cpf, that.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return "Funcionario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", crf='" + crf + '\'' +
                ", funcao=" + funcao +
                '}';
    }
}
