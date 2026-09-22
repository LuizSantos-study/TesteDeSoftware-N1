package farmaciavenda.models;

import farmaciavenda.enums.PlanoCliente;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade que representa um Cliente da farmácia.
 */
public class Cliente {

    private Long id;
    private String nome;
    private String cpf;
    private LocalDate dataNasc;
    private PlanoCliente plano;

    public Cliente() {
        this.plano = PlanoCliente.NENHUM;
    }

    public Cliente(Long id, String nome, String cpf, LocalDate dataNasc, PlanoCliente plano) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNasc = dataNasc;
        this.plano = plano != null ? plano : PlanoCliente.NENHUM;
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

    public LocalDate getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(LocalDate dataNasc) {
        this.dataNasc = dataNasc;
    }

    public PlanoCliente getPlano() {
        return plano;
    }

    public void setPlano(PlanoCliente plano) {
        this.plano = plano != null ? plano : PlanoCliente.NENHUM;
    }

    public boolean temPlano() {
        return this.plano != null && this.plano != PlanoCliente.NENHUM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(cpf, cliente.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", dataNasc=" + dataNasc +
                ", plano=" + plano +
                '}';
    }
}
