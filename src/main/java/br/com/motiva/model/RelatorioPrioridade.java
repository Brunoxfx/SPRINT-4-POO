package br.com.motiva.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_RELATORIO_PRIORIDADE")
public class RelatorioPrioridade {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqRelatorioPrioridade")
    @SequenceGenerator(name = "seqRelatorioPrioridade", sequenceName = "SQ_RELATORIO_PRIORIDADE",
            allocationSize = 1)
    @Column(name = "ID_RELATORIO")
    private Long id;

    @Column(name = "QUANTIDADE_URGENTE", nullable = false)
    private Integer quantidadeUrgente;

    @Column(name = "QUANTIDADE_CRITICO", nullable = false)
    private Integer quantidadeCritico;

    @Column(name = "QUANTIDADE_ATENCAO", nullable = false)
    private Integer quantidadeAtencao;

    @Column(name = "QUANTIDADE_NORMAL", nullable = false)
    private Integer quantidadeNormal;

    @Column(name = "RESUMO", nullable = false, length = 4000)
    private String resumo;

    @Column(name = "DATA_GERACAO", nullable = false)
    private LocalDateTime dataGeracao;

    protected RelatorioPrioridade() {
    }

    public RelatorioPrioridade(Integer quantidadeUrgente, Integer quantidadeCritico, Integer quantidadeAtencao,
            Integer quantidadeNormal, String resumo, LocalDateTime dataGeracao) {
        this.quantidadeUrgente = quantidadeUrgente;
        this.quantidadeCritico = quantidadeCritico;
        this.quantidadeAtencao = quantidadeAtencao;
        this.quantidadeNormal = quantidadeNormal;
        this.resumo = resumo;
        this.dataGeracao = dataGeracao;
    }

    public Long getId() {
        return this.id;
    }

    public Integer getQuantidadeUrgente() {
        return this.quantidadeUrgente;
    }

    public Integer getQuantidadeCritico() {
        return this.quantidadeCritico;
    }

    public Integer getQuantidadeAtencao() {
        return this.quantidadeAtencao;
    }

    public Integer getQuantidadeNormal() {
        return this.quantidadeNormal;
    }

    public String getResumo() {
        return this.resumo;
    }

    public LocalDateTime getDataGeracao() {
        return this.dataGeracao;
    }
}
