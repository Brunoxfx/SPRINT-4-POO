package br.com.motiva.dto;

import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.TrechoRodovia;

public record TrechoRodoviaResponse(Long id, Double kmInicial, Double kmFinal, String descricaoKm,
        Double alturaVegetacaoCm, String tipoAmbiente, String riscoOperacional, Long equipeId, String equipeNome) {

    public static TrechoRodoviaResponse de(TrechoRodovia trecho) {
        EquipeManutencao equipe = trecho.getEquipeManutencao();
        Long equipeId = equipe == null ? null : equipe.getId();
        String equipeNome = equipe == null ? null : equipe.getNome();
        return new TrechoRodoviaResponse(trecho.getId(), trecho.getKmInicial(), trecho.getKmFinal(),
                trecho.getDescricaoKm(), trecho.getAlturaVegetacaoCm(), trecho.getTipoAmbiente(),
                trecho.getRiscoOperacional(), equipeId, equipeNome);
    }
}
