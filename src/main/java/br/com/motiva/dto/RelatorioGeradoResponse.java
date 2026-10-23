package br.com.motiva.dto;

import java.util.ArrayList;
import java.util.List;

import br.com.motiva.service.ItemRelatorioPrioridade;
import br.com.motiva.service.RelatorioGerado;

public record RelatorioGeradoResponse(RelatorioPrioridadeResponse relatorio, List<ItemRelatorioResponse> itens) {
    public static RelatorioGeradoResponse de(RelatorioGerado gerado) {
        List<ItemRelatorioResponse> itens = new ArrayList<ItemRelatorioResponse>();
        for (ItemRelatorioPrioridade item : gerado.itens()) {
            itens.add(ItemRelatorioResponse.de(item));
        }

        return new RelatorioGeradoResponse(RelatorioPrioridadeResponse.de(gerado.relatorio()), itens);
    }
}
