package br.com.motiva.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.motiva.dto.RelatorioGeradoResponse;
import br.com.motiva.dto.RelatorioPrioridadeResponse;
import br.com.motiva.model.RelatorioPrioridade;
import br.com.motiva.service.GeradorRelatorio;
import br.com.motiva.service.RelatorioGerado;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioPrioridadeController {
    private final GeradorRelatorio geradorRelatorio;

    public RelatorioPrioridadeController(GeradorRelatorio geradorRelatorio) {
        this.geradorRelatorio = geradorRelatorio;
    }

    @PostMapping
    public ResponseEntity<RelatorioGeradoResponse> gerar() {
        RelatorioGerado gerado = this.geradorRelatorio.gerarRelatorio();
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(gerado.relatorio().getId()).toUri();
        return ResponseEntity.created(localizacao).body(RelatorioGeradoResponse.de(gerado));
    }

    @GetMapping
    public ResponseEntity<List<RelatorioPrioridadeResponse>> listarHistorico() {
        return ResponseEntity.ok(this.converter(this.geradorRelatorio.listarHistorico()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatorioPrioridadeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RelatorioPrioridadeResponse.de(this.geradorRelatorio.buscarPorId(id)));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<RelatorioPrioridadeResponse>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(this.converter(this.geradorRelatorio.buscarPorPeriodo(inicio, fim)));
    }

    private List<RelatorioPrioridadeResponse> converter(List<RelatorioPrioridade> relatorios) {
        List<RelatorioPrioridadeResponse> respostas = new ArrayList<RelatorioPrioridadeResponse>();
        for (RelatorioPrioridade relatorio : relatorios) {
            respostas.add(RelatorioPrioridadeResponse.de(relatorio));
        }
        return respostas;
    }
}
