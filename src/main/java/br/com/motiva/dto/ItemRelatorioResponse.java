package br.com.motiva.dto;

import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.service.ItemRelatorioPrioridade;

public record ItemRelatorioResponse(Long trechoId, String descricaoKm, Double alturaVegetacaoCm,
        String tipoAmbiente, String riscoOperacional, String prioridade, String tipoIntervencao,
        String intervencaoIndicada, String motivo) {
    public static final String SEM_INTERVENCAO = "nenhuma";

    public static ItemRelatorioResponse de(ItemRelatorioPrioridade item) {
        TrechoRodovia trecho = item.trecho();
        String intervencaoIndicada = item.precisaIntervencao() ? item.nomeIntervencao() : SEM_INTERVENCAO;

        return new ItemRelatorioResponse(trecho.getId(), trecho.getDescricaoKm(), trecho.getAlturaVegetacaoCm(),
                trecho.getTipoAmbiente(), trecho.getRiscoOperacional(), item.prioridade(), item.tipoIntervencao(),
                intervencaoIndicada, item.motivo());
    }
}
