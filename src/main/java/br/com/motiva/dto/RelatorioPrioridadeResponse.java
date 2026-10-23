package br.com.motiva.dto;

import java.time.LocalDateTime;

import br.com.motiva.model.RelatorioPrioridade;

public record RelatorioPrioridadeResponse(Long id, Integer quantidadeUrgente, Integer quantidadeCritico,
        Integer quantidadeAtencao, Integer quantidadeNormal, String resumo, LocalDateTime dataGeracao) {
    public static RelatorioPrioridadeResponse de(RelatorioPrioridade relatorio) {
        return new RelatorioPrioridadeResponse(relatorio.getId(), relatorio.getQuantidadeUrgente(),
                relatorio.getQuantidadeCritico(), relatorio.getQuantidadeAtencao(), relatorio.getQuantidadeNormal(),
                relatorio.getResumo(), relatorio.getDataGeracao());
    }
}
