package br.com.motiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IntervencaoOperacionalRequest(
        @NotBlank(message = "O tipo da intervencao deve ser informado.") String tipo,
        @NotNull(message = "O ID do trecho deve ser informado.") Long trechoId,
        Long equipeId) {
}
