package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "TB_TRECHO_RODOVIA", uniqueConstraints = @UniqueConstraint(name = "UK_TRECHO_KM",
        columnNames = { "KM_INICIAL", "KM_FINAL" }))
public class TrechoRodovia {
    public static final String AMBIENTE_SECO = "seco";
    public static final String AMBIENTE_UMIDO = "umido";

    public static final String RISCO_BAIXO = "baixo";
    public static final String RISCO_ALTO = "alto";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqTrechoRodovia")
    @SequenceGenerator(name = "seqTrechoRodovia", sequenceName = "SQ_TRECHO_RODOVIA", allocationSize = 1)
    @Column(name = "ID_TRECHO")
    private Long id;

    @Column(name = "KM_INICIAL", nullable = false)
    private Double kmInicial;

    @Column(name = "KM_FINAL", nullable = false)
    private Double kmFinal;

    @Column(name = "ALTURA_VEGETACAO_CM", nullable = false)
    private Double alturaVegetacaoCm;

    @Column(name = "TIPO_AMBIENTE", nullable = false, length = 20)
    private String tipoAmbiente;

    @Column(name = "RISCO_OPERACIONAL", nullable = false, length = 20)
    private String riscoOperacional;

    @ManyToOne
    @JoinColumn(name = "ID_EQUIPE")
    private EquipeManutencao equipeManutencao;

    protected TrechoRodovia() {
    }

    public TrechoRodovia(Double kmInicial, Double kmFinal, Double alturaVegetacaoCm, String tipoAmbiente,
            String riscoOperacional, EquipeManutencao equipeManutencao) {
        this.kmInicial = kmInicial;
        this.kmFinal = kmFinal;
        this.alturaVegetacaoCm = alturaVegetacaoCm;
        this.tipoAmbiente = tipoAmbiente;
        this.riscoOperacional = riscoOperacional;
        this.equipeManutencao = equipeManutencao;
    }

    public Long getId() {
        return this.id;
    }

    public Double getKmInicial() {
        return this.kmInicial;
    }

    public Double getKmFinal() {
        return this.kmFinal;
    }

    public Double getAlturaVegetacaoCm() {
        return this.alturaVegetacaoCm;
    }

    public String getTipoAmbiente() {
        return this.tipoAmbiente;
    }

    public String getRiscoOperacional() {
        return this.riscoOperacional;
    }

    public EquipeManutencao getEquipeManutencao() {
        return this.equipeManutencao;
    }

    public void setKmInicial(Double kmInicial) {
        this.kmInicial = kmInicial;
    }

    public void setKmFinal(Double kmFinal) {
        this.kmFinal = kmFinal;
    }

    public void setAlturaVegetacaoCm(Double alturaVegetacaoCm) {
        this.alturaVegetacaoCm = alturaVegetacaoCm;
    }

    public void setTipoAmbiente(String tipoAmbiente) {
        this.tipoAmbiente = tipoAmbiente;
    }

    public void setRiscoOperacional(String riscoOperacional) {
        this.riscoOperacional = riscoOperacional;
    }

    public void setEquipeManutencao(EquipeManutencao equipeManutencao) {
        this.equipeManutencao = equipeManutencao;
    }

    public String getDescricaoKm() {
        return "KM " + this.kmInicial + " ao KM " + this.kmFinal;
    }

    public boolean isRiscoAlto() {
        return RISCO_ALTO.equalsIgnoreCase(this.riscoOperacional);
    }
}
