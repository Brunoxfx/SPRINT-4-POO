package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_EQUIPE_MANUTENCAO")
public class EquipeManutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqEquipeManutencao")
    @SequenceGenerator(name = "seqEquipeManutencao", sequenceName = "SQ_EQUIPE_MANUTENCAO", allocationSize = 1)
    @Column(name = "ID_EQUIPE")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100, unique = true)
    private String nome;

    @Column(name = "QUANTIDADE_INTEGRANTES", nullable = false)
    private Integer quantidadeIntegrantes;

    @Column(name = "ESPECIALIDADE", nullable = false, length = 100)
    private String especialidade;

    protected EquipeManutencao() {
    }

    public EquipeManutencao(String nome, Integer quantidadeIntegrantes, String especialidade) {
        this.nome = nome;
        this.quantidadeIntegrantes = quantidadeIntegrantes;
        this.especialidade = especialidade;
    }

    public Long getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public Integer getQuantidadeIntegrantes() {
        return this.quantidadeIntegrantes;
    }

    public String getEspecialidade() {
        return this.especialidade;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setQuantidadeIntegrantes(Integer quantidadeIntegrantes) {
        this.quantidadeIntegrantes = quantidadeIntegrantes;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String obterResumo() {
        return this.nome + " (" + this.quantidadeIntegrantes + " integrantes, " + this.especialidade + ")";
    }
}
