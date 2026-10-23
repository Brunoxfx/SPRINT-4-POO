package br.com.motiva.service;

import br.com.motiva.model.TrechoRodovia;

public record ItemRelatorioPrioridade(TrechoRodovia trecho, String prioridade, String tipoIntervencao,
        String nomeIntervencao, String motivo) {
    public boolean precisaIntervencao() {
        return this.tipoIntervencao != null;
    }
}
