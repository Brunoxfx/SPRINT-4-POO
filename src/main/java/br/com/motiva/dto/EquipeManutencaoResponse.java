package br.com.motiva.dto;

import br.com.motiva.model.EquipeManutencao;

public record EquipeManutencaoResponse(Long id, String nome, Integer quantidadeIntegrantes, String especialidade) {
    public static EquipeManutencaoResponse de(EquipeManutencao equipe) {
        return new EquipeManutencaoResponse(equipe.getId(), equipe.getNome(), equipe.getQuantidadeIntegrantes(),
                equipe.getEspecialidade());
    }
}
