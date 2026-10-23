package br.com.motiva.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(LocalDateTime dataHora, int status, String erro, String mensagem, String caminho,
        List<String> detalhes) {
}
