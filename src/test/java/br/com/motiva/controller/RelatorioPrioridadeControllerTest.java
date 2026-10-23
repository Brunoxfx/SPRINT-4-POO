package br.com.motiva.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.TrechoRodoviaRepository;
import br.com.motiva.service.MotorPrioridade;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelatorioPrioridadeControllerTest {
    private static final String URL = "/api/relatorios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrechoRodoviaRepository trechoRepository;

    private TrechoRodovia trechoUrgente;
    private TrechoRodovia trechoNormal;

    @BeforeEach
    void criarTrechos() {
        this.trechoUrgente = this.trechoRepository.save(new TrechoRodovia(900.0, 901.0, 92.0,
                TrechoRodovia.AMBIENTE_UMIDO, TrechoRodovia.RISCO_ALTO, null));
        this.trechoRepository.save(new TrechoRodovia(902.0, 903.0, 85.0, TrechoRodovia.AMBIENTE_SECO,
                TrechoRodovia.RISCO_BAIXO, null));
        this.trechoRepository.save(new TrechoRodovia(904.0, 905.0, 50.0, TrechoRodovia.AMBIENTE_SECO,
                TrechoRodovia.RISCO_ALTO, null));
        this.trechoNormal = this.trechoRepository.save(new TrechoRodovia(906.0, 907.0, 10.0,
                TrechoRodovia.AMBIENTE_SECO, TrechoRodovia.RISCO_BAIXO, null));
    }

    @Test
    void deveGerarRelatorioComLocationContagensResumoEItens() throws Exception {
        String filtroUrgente = "$.itens[?(@.trechoId == " + this.trechoUrgente.getId() + ")]";
        String filtroNormal = "$.itens[?(@.trechoId == " + this.trechoNormal.getId() + ")]";

        MvcResult resultado = this.mockMvc.perform(post(URL))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.relatorio.id").isNumber())
                .andExpect(jsonPath("$.relatorio.quantidadeUrgente").value(1))
                .andExpect(jsonPath("$.relatorio.quantidadeCritico").value(1))
                .andExpect(jsonPath("$.relatorio.quantidadeAtencao").value(1))
                .andExpect(jsonPath("$.relatorio.quantidadeNormal").value(1))
                .andExpect(jsonPath("$.relatorio.resumo").value("Urgente: 1, Critico: 1, Atencao: 1, Normal: 1"))
                .andExpect(jsonPath("$.relatorio.dataGeracao").exists())
                .andExpect(jsonPath("$.itens.length()").value(4))
                .andExpect(jsonPath(filtroUrgente + ".prioridade")
                        .value(hasItem(MotorPrioridade.PRIORIDADE_URGENTE)))
                .andExpect(jsonPath(filtroUrgente + ".tipoIntervencao")
                        .value(hasItem(IntervencaoOperacional.TIPO_ROCADA_MANUAL)))
                .andExpect(jsonPath(filtroUrgente + ".intervencaoIndicada").value(hasItem("Rocada manual")))
                .andExpect(jsonPath(filtroNormal + ".prioridade")
                        .value(hasItem(MotorPrioridade.PRIORIDADE_NORMAL)))
                .andExpect(jsonPath(filtroNormal + ".intervencaoIndicada").value(hasItem("nenhuma")))
                .andReturn();

        Integer id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.relatorio.id");
        assertThat(resultado.getResponse().getHeader("Location"), endsWith("/api/relatorios/" + id));
    }

    @Test
    void deveListarHistoricoContendoRelatorioGerado() throws Exception {
        Integer id = this.gerarRelatorio();

        this.mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(id)));
    }

    @Test
    void deveBuscarRelatorioPorId() throws Exception {
        Integer id = this.gerarRelatorio();

        this.mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.resumo").value("Urgente: 1, Critico: 1, Atencao: 1, Normal: 1"));
    }

    @Test
    void deveRetornar404ParaRelatorioInexistente() throws Exception {
        this.mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Relatorio de prioridade com ID 999999 nao encontrado."));
    }

    @Test
    void deveConsultarRelatoriosDoPeriodoAtual() throws Exception {
        Integer id = this.gerarRelatorio();
        String hoje = LocalDate.now().toString();

        this.mockMvc.perform(get(URL + "/periodo").param("inicio", hoje).param("fim", hoje))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(id)));
    }

    @Test
    void deveRetornarListaVaziaParaPeriodoSemRelatorios() throws Exception {
        this.gerarRelatorio();

        this.mockMvc.perform(get(URL + "/periodo").param("inicio", "2000-01-01").param("fim", "2000-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornar400QuandoInicioPosteriorAoFim() throws Exception {
        this.mockMvc.perform(get(URL + "/periodo").param("inicio", "2026-10-10").param("fim", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("A data de inicio deve ser anterior ou igual a data de fim."));
    }

    @Test
    void deveRetornar400QuandoFimNaoInformado() throws Exception {
        this.mockMvc.perform(get(URL + "/periodo").param("inicio", "2026-10-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoDataMalFormatada() throws Exception {
        this.mockMvc.perform(get(URL + "/periodo").param("inicio", "01/10/2026").param("fim", "2026-10-31"))
                .andExpect(status().isBadRequest());
    }

    private Integer gerarRelatorio() throws Exception {
        MvcResult resultado = this.mockMvc.perform(post(URL))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.relatorio.id");
    }
}
