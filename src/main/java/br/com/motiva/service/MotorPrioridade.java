package br.com.motiva.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.TrechoRodovia;

@Service
public class MotorPrioridade {
    public static final String PRIORIDADE_URGENTE = "URGENTE";
    public static final String PRIORIDADE_CRITICO = "CRITICO";
    public static final String PRIORIDADE_ATENCAO = "ATENCAO";
    public static final String PRIORIDADE_NORMAL = "NORMAL";

    public static final String NOME_ROCADA_MANUAL = "Rocada manual";
    public static final String NOME_ROCADA_MECANIZADA = "Rocada mecanizada";
    public static final String NOME_PULVERIZACAO = "Pulverizacao";

    public static final double ALTURA_ALTA_CM = 80.0;
    public static final double ALTURA_MEDIA_CM = 40.0;

    public List<ItemRelatorioPrioridade> gerarRelatorio(List<TrechoRodovia> trechos) {
        List<ItemRelatorioPrioridade> itens = new ArrayList<ItemRelatorioPrioridade>();

        for (TrechoRodovia trecho : trechos) {
            itens.add(this.avaliarTrecho(trecho));
        }

        return itens;
    }

    public ItemRelatorioPrioridade avaliarTrecho(TrechoRodovia trecho) {
        double altura = trecho.getAlturaVegetacaoCm();
        String tipoIntervencao;
        String nomeIntervencao;
        String prioridade;
        String motivo;

        if (altura >= ALTURA_ALTA_CM && trecho.isRiscoAlto()) {
            tipoIntervencao = IntervencaoOperacional.TIPO_ROCADA_MANUAL;
            nomeIntervencao = NOME_ROCADA_MANUAL;
            prioridade = PRIORIDADE_URGENTE;
            motivo = "Vegetacao alta em area com risco operacional alto.";
        } else if (altura >= ALTURA_ALTA_CM) {
            tipoIntervencao = IntervencaoOperacional.TIPO_ROCADA_MECANIZADA;
            nomeIntervencao = NOME_ROCADA_MECANIZADA;
            prioridade = PRIORIDADE_CRITICO;
            motivo = "Vegetacao alta em area com risco operacional baixo.";
        } else if (altura >= ALTURA_MEDIA_CM) {
            tipoIntervencao = IntervencaoOperacional.TIPO_PULVERIZACAO;
            nomeIntervencao = NOME_PULVERIZACAO;
            prioridade = PRIORIDADE_ATENCAO;
            motivo = "Vegetacao media exige controle preventivo.";
        } else {
            tipoIntervencao = null;
            nomeIntervencao = null;
            prioridade = PRIORIDADE_NORMAL;
            motivo = "Vegetacao baixa. Nao ha prioridade de servico.";
        }

        return new ItemRelatorioPrioridade(trecho, prioridade, tipoIntervencao, nomeIntervencao, motivo);
    }
}
