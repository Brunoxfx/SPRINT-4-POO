package br.com.motiva.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.repository.EquipeManutencaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrechoRodoviaControllerTest {
    private static final String URL = "/api/trechos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipeManutencaoRepository equipeRepository;

    @Test
    void deveCadastrarTrechoERetornar201ComLocation() throws Exception {
        String json = """
                {
                  "kmInicial": 500.0,
                  "kmFinal": 505.5,
                  "alturaVegetacaoCm": 85.0,
                  "tipoAmbiente": "UMIDO",
                  "riscoOperacional": "Alto"
                }
                """;

        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/trechos/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.kmInicial").value(500.0))
                .andExpect(jsonPath("$.kmFinal").value(505.5))
                .andExpect(jsonPath("$.descricaoKm").value("KM 500.0 ao KM 505.5"))
                .andExpect(jsonPath("$.alturaVegetacaoCm").value(85.0))
                .andExpect(jsonPath("$.tipoAmbiente").value("umido"))
                .andExpect(jsonPath("$.riscoOperacional").value("alto"))
                .andExpect(jsonPath("$.equipeId").doesNotExist())
                .andExpect(jsonPath("$.equipeNome").doesNotExist());
    }

    @Test
    void deveCadastrarTrechoVinculadoAEquipe() throws Exception {
        EquipeManutencao equipe = this.equipeRepository.save(
                new EquipeManutencao("Equipe Teste Trecho", 4, "Rocada"));
        String json = """
                {
                  "kmInicial": 512.0,
                  "kmFinal": 513.0,
                  "alturaVegetacaoCm": 30.0,
                  "tipoAmbiente": "seco",
                  "riscoOperacional": "baixo",
                  "equipeId": %d
                }
                """.formatted(equipe.getId());

        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipeId").value(equipe.getId()))
                .andExpect(jsonPath("$.equipeNome").value("Equipe Teste Trecho"));
    }

    @Test
    void deveListarTrechosComStatus200() throws Exception {
        this.cadastrarTrecho(520.0, 521.0, 10.0);

        this.mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].kmInicial", hasItem(520.0)));
    }

    @Test
    void deveBuscarTrechoPorIdComStatus200() throws Exception {
        Long id = this.cadastrarTrecho(530.0, 531.0, 45.0);

        this.mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.kmInicial").value(530.0))
                .andExpect(jsonPath("$.tipoAmbiente").value("seco"));
    }

    @Test
    void deveRetornar404AoBuscarTrechoInexistente() throws Exception {
        this.mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Trecho de rodovia com ID 999999 nao encontrado."));
    }

    @Test
    void deveAtualizarTrechoComStatus200() throws Exception {
        Long id = this.cadastrarTrecho(540.0, 541.0, 20.0);
        String json = """
                {
                  "kmInicial": 540.0,
                  "kmFinal": 542.0,
                  "alturaVegetacaoCm": 90.0,
                  "tipoAmbiente": "umido",
                  "riscoOperacional": "ALTO"
                }
                """;

        this.mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.kmFinal").value(542.0))
                .andExpect(jsonPath("$.alturaVegetacaoCm").value(90.0))
                .andExpect(jsonPath("$.tipoAmbiente").value("umido"))
                .andExpect(jsonPath("$.riscoOperacional").value("alto"));
    }

    @Test
    void deveRetornar404AoAtualizarTrechoInexistente() throws Exception {
        this.mockMvc.perform(put(URL + "/999999").contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(550.0, 551.0, 10.0)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRemoverTrechoComStatus204EDepoisRetornar404() throws Exception {
        Long id = this.cadastrarTrecho(560.0, 561.0, 15.0);

        this.mockMvc.perform(delete(URL + "/" + id))
                .andExpect(status().isNoContent());

        this.mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoRemoverTrechoInexistente() throws Exception {
        this.mockMvc.perform(delete(URL + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoAlturaForNegativa() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(570.0, 571.0, -5.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A altura da vegetacao nao pode ser negativa."));
    }

    @Test
    void deveRetornar400QuandoKmFinalNaoForMaiorQueInicial() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(580.0, 575.0, 10.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O KM final deve ser maior que o KM inicial."));
    }

    @Test
    void deveRetornar400QuandoAmbienteForInvalido() throws Exception {
        String json = """
                {
                  "kmInicial": 590.0,
                  "kmFinal": 591.0,
                  "alturaVegetacaoCm": 10.0,
                  "tipoAmbiente": "chuvoso",
                  "riscoOperacional": "baixo"
                }
                """;

        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O ambiente deve ser seco ou umido."));
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosEstiveremAusentes() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados invalidos na requisicao."))
                .andExpect(jsonPath("$.detalhes", hasSize(5)));
    }

    @Test
    void deveRetornar409QuandoParDeKmJaExistir() throws Exception {
        this.cadastrarTrecho(600.0, 601.0, 10.0);

        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(600.0, 601.0, 50.0)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404QuandoEquipeNaoExistir() throws Exception {
        String json = """
                {
                  "kmInicial": 610.0,
                  "kmFinal": 611.0,
                  "alturaVegetacaoCm": 10.0,
                  "tipoAmbiente": "seco",
                  "riscoOperacional": "baixo",
                  "equipeId": 999999
                }
                """;

        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Equipe de manutencao com ID 999999 nao encontrada."));
    }

    @Test
    void deveFiltrarTrechosPorAlturaMinima() throws Exception {
        this.cadastrarTrecho(620.0, 621.0, 30.0);
        this.cadastrarTrecho(622.0, 623.0, 80.0);
        this.cadastrarTrecho(624.0, 625.0, 120.0);

        this.mockMvc.perform(get(URL).param("alturaMinima", "80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].alturaVegetacaoCm", everyItem(greaterThanOrEqualTo(80.0))))
                .andExpect(jsonPath("$[*].kmInicial", hasItem(622.0)))
                .andExpect(jsonPath("$[*].kmInicial", hasItem(624.0)))
                .andExpect(jsonPath("$[*].kmInicial", not(hasItem(620.0))));
    }

    @Test
    void deveRetornar400QuandoAlturaNaoForNumeroValido() throws Exception {
        String json = """
                {
                  "kmInicial": 630.0,
                  "kmFinal": 631.0,
                  "alturaVegetacaoCm": "NaN",
                  "tipoAmbiente": "seco",
                  "riscoOperacional": "baixo"
                }
                """;

        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A altura da vegetacao deve ser um numero valido."));
    }

    @Test
    void deveRetornar400QuandoKmTiverMaisDeDuasCasasDecimais() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(10.001, 10.002, 50.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O KM inicial deve ter no maximo duas casas decimais."));
    }

    @Test
    void deveRetornar400QuandoAlturaUltrapassarLimiteDaColuna() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(632.0, 633.0, 100000000.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A altura da vegetacao deve ser no maximo 99999999.99."));
    }

    @Test
    void deveRetornar400AoFiltrarComAlturaMinimaInvalida() throws Exception {
        this.mockMvc.perform(get(URL).param("alturaMinima", "NaN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A altura minima deve ser um numero valido."));
    }

    private Long cadastrarTrecho(Double kmInicial, Double kmFinal, Double altura) throws Exception {
        MvcResult resultado = this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(this.montarJson(kmInicial, kmFinal, altura)))
                .andExpect(status().isCreated())
                .andReturn();
        String localizacao = resultado.getResponse().getHeader("Location");
        return Long.valueOf(localizacao.substring(localizacao.lastIndexOf('/') + 1));
    }

    private String montarJson(Double kmInicial, Double kmFinal, Double altura) {
        return """
                {
                  "kmInicial": %s,
                  "kmFinal": %s,
                  "alturaVegetacaoCm": %s,
                  "tipoAmbiente": "seco",
                  "riscoOperacional": "baixo"
                }
                """.formatted(kmInicial, kmFinal, altura);
    }
}
