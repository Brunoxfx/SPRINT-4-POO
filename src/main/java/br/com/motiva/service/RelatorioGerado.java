package br.com.motiva.service;

import java.util.List;

import br.com.motiva.model.RelatorioPrioridade;

public record RelatorioGerado(RelatorioPrioridade relatorio, List<ItemRelatorioPrioridade> itens) {
}
