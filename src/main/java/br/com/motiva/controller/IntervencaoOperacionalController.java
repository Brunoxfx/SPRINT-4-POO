package br.com.motiva.controller;

import java.net.URI;
import java.util.ArrayList;
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

import br.com.motiva.dto.IntervencaoOperacionalRequest;
import br.com.motiva.dto.IntervencaoOperacionalResponse;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.service.IntervencaoOperacionalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/intervencoes")
public class IntervencaoOperacionalController {
    private final IntervencaoOperacionalService intervencaoService;

    public IntervencaoOperacionalController(IntervencaoOperacionalService intervencaoService) {
        this.intervencaoService = intervencaoService;
    }

    @GetMapping
    public ResponseEntity<List<IntervencaoOperacionalResponse>> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long trechoId,
            @RequestParam(required = false) Long equipeId) {
        List<IntervencaoOperacional> intervencoes;
        if (tipo != null) {
            intervencoes = this.intervencaoService.listarPorTipo(tipo);
        } else if (trechoId != null) {
            intervencoes = this.intervencaoService.listarPorTrecho(trechoId);
        } else if (equipeId != null) {
            intervencoes = this.intervencaoService.listarPorEquipe(equipeId);
        } else {
            intervencoes = this.intervencaoService.listarTodas();
        }
        return ResponseEntity.ok(this.converterLista(intervencoes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntervencaoOperacionalResponse> buscarPorId(@PathVariable Long id) {
        IntervencaoOperacional intervencao = this.intervencaoService.buscarPorId(id);
        return ResponseEntity.ok(IntervencaoOperacionalResponse.de(intervencao));
    }

    @PostMapping
    public ResponseEntity<IntervencaoOperacionalResponse> cadastrar(
            @Valid @RequestBody IntervencaoOperacionalRequest request) {
        IntervencaoOperacional intervencao = this.intervencaoService.cadastrar(request);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(intervencao.getId())
                .toUri();
        return ResponseEntity.created(localizacao).body(IntervencaoOperacionalResponse.de(intervencao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IntervencaoOperacionalResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody IntervencaoOperacionalRequest request) {
        IntervencaoOperacional intervencao = this.intervencaoService.atualizar(id, request);
        return ResponseEntity.ok(IntervencaoOperacionalResponse.de(intervencao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        this.intervencaoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private List<IntervencaoOperacionalResponse> converterLista(List<IntervencaoOperacional> intervencoes) {
        List<IntervencaoOperacionalResponse> respostas = new ArrayList<IntervencaoOperacionalResponse>();
        for (IntervencaoOperacional intervencao : intervencoes) {
            respostas.add(IntervencaoOperacionalResponse.de(intervencao));
        }
        return respostas;
    }
}
