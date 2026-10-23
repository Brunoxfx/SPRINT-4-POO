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

@Entity
@Table(name = "TB_INTERVENCAO_OPERACIONAL")
public class IntervencaoOperacional {
    public static final String TIPO_ROCADA_MANUAL = "ROCADA_MANUAL";
    public static final String TIPO_ROCADA_MECANIZADA = "ROCADA_MECANIZADA";
    public static final String TIPO_PULVERIZACAO = "PULVERIZACAO";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqIntervencaoOperacional")
    @SequenceGenerator(name = "seqIntervencaoOperacional", sequenceName = "SQ_INTERVENCAO_OPERACIONAL",
            allocationSize = 1)
    @Column(name = "ID_INTERVENCAO")
    private Long id;

    @Column(name = "TIPO", nullable = false, length = 30)
    private String tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_TRECHO", nullable = false)
    private TrechoRodovia trecho;

    @ManyToOne
    @JoinColumn(name = "ID_EQUIPE")
    private EquipeManutencao equipeManutencao;

    protected IntervencaoOperacional() {
    }

    public IntervencaoOperacional(String tipo, TrechoRodovia trecho, EquipeManutencao equipeManutencao) {
        this.tipo = tipo;
        this.trecho = trecho;
        this.equipeManutencao = equipeManutencao;
    }

    public Long getId() {
        return this.id;
    }

    public String getTipo() {
        return this.tipo;
    }

    public TrechoRodovia getTrecho() {
        return this.trecho;
    }

    public EquipeManutencao getEquipeManutencao() {
        return this.equipeManutencao;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setTrecho(TrechoRodovia trecho) {
        this.trecho = trecho;
    }

    public void setEquipeManutencao(EquipeManutencao equipeManutencao) {
        this.equipeManutencao = equipeManutencao;
    }

    public static boolean isTipoValido(String tipo) {
        return TIPO_ROCADA_MANUAL.equals(tipo) || TIPO_ROCADA_MECANIZADA.equals(tipo)
                || TIPO_PULVERIZACAO.equals(tipo);
    }
}
