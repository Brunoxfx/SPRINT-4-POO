package br.com.motiva.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import br.com.motiva.dto.TrechoRodoviaRequest;
import br.com.motiva.dto.TrechoRodoviaResponse;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.service.TrechoRodoviaService;

@RestController
@RequestMapping("/api/trechos")
public class TrechoRodoviaController {
    private final TrechoRodoviaService trechoService;

    public TrechoRodoviaController(TrechoRodoviaService trechoService) {
        this.trechoService = trechoService;
    }

    @GetMapping
    public ResponseEntity<List<TrechoRodoviaResponse>> listar(
            @RequestParam(required = false) Double alturaMinima,
            @RequestParam(required = false) String risco,
            @RequestParam(required = false) String ambiente,
            @RequestParam(required = false) Long equipeId) {
        List<TrechoRodovia> trechos;
        if (alturaMinima != null) {
            trechos = this.trechoService.listarPorAlturaMinima(alturaMinima);
        } else if (risco != null) {
            trechos = this.trechoService.listarPorRisco(risco);
        } else if (ambiente != null) {
            trechos = this.trechoService.listarPorAmbiente(ambiente);
        } else if (equipeId != null) {
            trechos = this.trechoService.listarPorEquipe(equipeId);
        } else {
            trechos = this.trechoService.listarTodos();
        }
        return ResponseEntity.ok(this.converter(trechos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrechoRodoviaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(TrechoRodoviaResponse.de(this.trechoService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<TrechoRodoviaResponse> cadastrar(@Valid @RequestBody TrechoRodoviaRequest requisicao) {
        TrechoRodovia trecho = this.trechoService.cadastrar(requisicao);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(trecho.getId()).toUri();
        return ResponseEntity.created(localizacao).body(TrechoRodoviaResponse.de(trecho));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrechoRodoviaResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody TrechoRodoviaRequest requisicao) {
        return ResponseEntity.ok(TrechoRodoviaResponse.de(this.trechoService.atualizar(id, requisicao)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        this.trechoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private List<TrechoRodoviaResponse> converter(List<TrechoRodovia> trechos) {
        return trechos.stream().map(TrechoRodoviaResponse::de).toList();
    }
}
