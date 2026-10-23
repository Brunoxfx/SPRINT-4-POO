package br.com.motiva.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
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

import br.com.motiva.dto.EquipeManutencaoRequest;
import br.com.motiva.dto.EquipeManutencaoResponse;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.service.EquipeManutencaoService;

@RestController
@RequestMapping("/api/equipes")
public class EquipeManutencaoController {
    private final EquipeManutencaoService equipeService;

    public EquipeManutencaoController(EquipeManutencaoService equipeService) {
        this.equipeService = equipeService;
    }

    @GetMapping
    public ResponseEntity<List<EquipeManutencaoResponse>> listar(
            @RequestParam(required = false) String especialidade) {
        List<EquipeManutencao> equipes;
        if (especialidade == null) {
            equipes = this.equipeService.listarTodas();
        } else {
            equipes = this.equipeService.listarPorEspecialidade(especialidade);
        }
        return ResponseEntity.ok(this.converter(equipes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeManutencaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(EquipeManutencaoResponse.de(this.equipeService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<EquipeManutencaoResponse> cadastrar(@Valid @RequestBody EquipeManutencaoRequest request) {
        EquipeManutencao equipe = this.equipeService.cadastrar(request);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(equipe.getId()).toUri();
        return ResponseEntity.created(localizacao).body(EquipeManutencaoResponse.de(equipe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipeManutencaoResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody EquipeManutencaoRequest request) {
        return ResponseEntity.ok(EquipeManutencaoResponse.de(this.equipeService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        this.equipeService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private List<EquipeManutencaoResponse> converter(List<EquipeManutencao> equipes) {
        List<EquipeManutencaoResponse> respostas = new ArrayList<EquipeManutencaoResponse>();
        for (EquipeManutencao equipe : equipes) {
            respostas.add(EquipeManutencaoResponse.de(equipe));
        }
        return respostas;
    }
}
