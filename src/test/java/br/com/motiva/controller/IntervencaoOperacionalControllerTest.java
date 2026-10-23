package br.com.motiva.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntervencaoOperacionalControllerTest {
    private static final String URL_INTERVENCOES = "/api/intervencoes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrechoRodoviaRepository trechoRepository;

    @Autowired
    private EquipeManutencaoRepository equipeRepository;

    private Long trechoId;
    private Long outroTrechoId;
    private Long equipeId;

    @BeforeEach
    void prepararDados() {
        EquipeManutencao equipe = this.equipeRepository.save(
                new EquipeManutencao("Equipe Teste Intervencao", 5, "Rocada e pulverizacao"));
        TrechoRodovia trecho = this.trechoRepository.save(
                new TrechoRodovia(700.0, 701.0, 90.0, TrechoRodovia.AMBIENTE_SECO, TrechoRodovia.RISCO_ALTO, equipe));
        TrechoRodovia outroTrecho = this.trechoRepository.save(
                new TrechoRodovia(702.0, 703.0, 50.0, TrechoRodovia.AMBIENTE_UMIDO, TrechoRodovia.RISCO_BAIXO,
                        null));
        this.equipeId = equipe.getId();
        this.trechoId = trecho.getId();
        this.outroTrechoId = outroTrecho.getId();
    }

    @Test
    void deveCadastrarIntervencaoERetornar201ComLocation() throws Exception {
        String json = """
                {"tipo": "pulverizacao", "trechoId": %d, "equipeId": %d}
                """.formatted(this.trechoId, this.equipeId);

        this.mockMvc.perform(post(URL_INTERVENCOES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tipo").value("PULVERIZACAO"))
                .andExpect(jsonPath("$.nomeIntervencao").value("Pulverizacao"))
                .andExpect(jsonPath("$.trechoId").value(this.trechoId))
                .andExpect(jsonPath("$.descricaoTrecho").value("KM 700.0 ao KM 701.0"))
                .andExpect(jsonPath("$.equipeId").value(this.equipeId))
                .andExpect(jsonPath("$.equipeNome").value("Equipe Teste Intervencao"));
    }

    @Test
    void deveListarIntervencoesComStatus200() throws Exception {
        Long id = this.cadastrar("ROCADA_MANUAL", this.trechoId, null);

        this.mockMvc.perform(get(URL_INTERVENCOES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())));
    }

    @Test
    void deveBuscarIntervencaoPorIdComStatus200() throws Exception {
        Long id = this.cadastrar("ROCADA_MECANIZADA", this.trechoId, this.equipeId);

        this.mockMvc.perform(get(URL_INTERVENCOES + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.tipo").value("ROCADA_MECANIZADA"))
                .andExpect(jsonPath("$.nomeIntervencao").value("Rocada mecanizada"));
    }

    @Test
    void deveRetornar404AoBuscarIntervencaoInexistente() throws Exception {
        this.mockMvc.perform(get(URL_INTERVENCOES + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Intervencao operacional com ID 999999 nao encontrada."));
    }

    @Test
    void deveAtualizarTipoDaIntervencaoComStatus200() throws Exception {
        Long id = this.cadastrar("ROCADA_MANUAL", this.trechoId, this.equipeId);
        String json = """
                {"tipo": "ROCADA_MECANIZADA", "trechoId": %d}
                """.formatted(this.outroTrechoId);

        this.mockMvc.perform(put(URL_INTERVENCOES + "/" + id).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.tipo").value("ROCADA_MECANIZADA"))
                .andExpect(jsonPath("$.nomeIntervencao").value("Rocada mecanizada"))
                .andExpect(jsonPath("$.trechoId").value(this.outroTrechoId))
                .andExpect(jsonPath("$.equipeId").doesNotExist());
    }

    @Test
    void deveRetornar404AoAtualizarIntervencaoInexistente() throws Exception {
        String json = """
                {"tipo": "PULVERIZACAO", "trechoId": %d}
                """.formatted(this.trechoId);

        this.mockMvc.perform(put(URL_INTERVENCOES + "/999999").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRemoverIntervencaoComStatus204EDepoisRetornar404() throws Exception {
        Long id = this.cadastrar("PULVERIZACAO", this.trechoId, null);

        this.mockMvc.perform(delete(URL_INTERVENCOES + "/" + id))
                .andExpect(status().isNoContent());

        this.mockMvc.perform(get(URL_INTERVENCOES + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoRemoverIntervencaoInexistente() throws Exception {
        this.mockMvc.perform(delete(URL_INTERVENCOES + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaTipoInvalido() throws Exception {
        String json = """
                {"tipo": "CAPINA", "trechoId": %d}
                """.formatted(this.trechoId);

        this.mockMvc.perform(post(URL_INTERVENCOES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("O tipo da intervencao deve ser ROCADA_MANUAL, ROCADA_MECANIZADA ou PULVERIZACAO."));
    }

    @Test
    void deveRetornar400QuandoTrechoNaoForInformado() throws Exception {
        String json = """
                {"tipo": "ROCADA_MANUAL"}
                """;

        this.mockMvc.perform(post(URL_INTERVENCOES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasItem("trechoId: O ID do trecho deve ser informado.")));
    }

    @Test
    void deveRetornar404QuandoTrechoNaoExistir() throws Exception {
        String json = """
                {"tipo": "ROCADA_MANUAL", "trechoId": 999999}
                """;

        this.mockMvc.perform(post(URL_INTERVENCOES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Trecho de rodovia com ID 999999 nao encontrado."));
    }

    @Test
    void deveRetornar404QuandoEquipeInformadaNaoExistir() throws Exception {
        String json = """
                {"tipo": "ROCADA_MANUAL", "trechoId": %d, "equipeId": 999999}
                """.formatted(this.trechoId);

        this.mockMvc.perform(post(URL_INTERVENCOES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Equipe de manutencao com ID 999999 nao encontrada."));
    }

    @Test
    void deveFiltrarIntervencoesPorTipo() throws Exception {
        Long idManual = this.cadastrar("ROCADA_MANUAL", this.trechoId, null);
        Long idPulverizacao = this.cadastrar("PULVERIZACAO", this.outroTrechoId, null);

        this.mockMvc.perform(get(URL_INTERVENCOES).param("tipo", "ROCADA_MANUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(hasSize(0))))
                .andExpect(jsonPath("$[*].tipo", everyItem(is("ROCADA_MANUAL"))))
                .andExpect(jsonPath("$[*].id", hasItem(idManual.intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(idPulverizacao.intValue()))));
    }

    @Test
    void deveRetornar400AoFiltrarPorTipoInvalido() throws Exception {
        this.mockMvc.perform(get(URL_INTERVENCOES).param("tipo", "xyz"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFiltrarIntervencoesPorTrecho() throws Exception {
        Long idTrecho = this.cadastrar("ROCADA_MANUAL", this.trechoId, null);
        Long idOutroTrecho = this.cadastrar("PULVERIZACAO", this.outroTrechoId, null);

        this.mockMvc.perform(get(URL_INTERVENCOES).param("trechoId", String.valueOf(this.trechoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(idTrecho))
                .andExpect(jsonPath("$[*].id", not(hasItem(idOutroTrecho.intValue()))));
    }

    private Long cadastrar(String tipo, Long trecho, Long equipe) throws Exception {
        String json = """
                {"tipo": "%s", "trechoId": %d, "equipeId": %s}
                """.formatted(tipo, trecho, equipe);

        MvcResult resultado = this.mockMvc
                .perform(post(URL_INTERVENCOES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String localizacao = resultado.getResponse().getHeader("Location");
        return Long.valueOf(localizacao.substring(localizacao.lastIndexOf('/') + 1));
    }
}
