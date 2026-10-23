package br.com.motiva.service;

public record ResumoPrioridades(int quantidadeUrgente, int quantidadeCritico, int quantidadeAtencao,
        int quantidadeNormal, String texto) {
}
