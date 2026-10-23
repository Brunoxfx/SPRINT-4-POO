package br.com.motiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipeManutencaoRequest(
        @NotBlank(message = "O nome da equipe deve ser informado.")
        @Size(max = 100, message = "O nome da equipe deve ter no maximo 100 caracteres.")
        String nome,

        @NotNull(message = "A quantidade de integrantes deve ser informada.")
        @Positive(message = "A equipe deve ter pelo menos um integrante.")
        Integer quantidadeIntegrantes,

        @NotBlank(message = "A especialidade deve ser informada.")
        @Size(max = 100, message = "A especialidade deve ter no maximo 100 caracteres.")
        String especialidade) {
}
