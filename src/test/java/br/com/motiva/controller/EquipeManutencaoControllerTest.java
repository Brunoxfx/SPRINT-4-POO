package br.com.motiva.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EquipeManutencaoControllerTest {
    private static final String URL_EQUIPES = "/api/equipes";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarEquipeERetornar201ComLocationECorpo() throws Exception {
        String json = """
                {
                  "nome": "Equipe Cadastro Teste",
                  "quantidadeIntegrantes": 5,
                  "especialidade": "Rocada manual"
                }
                """;

        this.mockMvc.perform(MockMvcRequestBuilders.post(URL_EQUIPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location",
                        Matchers.matchesPattern(".*/api/equipes/\\d+$")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nome").value("Equipe Cadastro Teste"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantidadeIntegrantes").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.especialidade").value("Rocada manual"));
    }

    @Test
    void deveListarEquipesERetornar200() throws Exception {
        this.criarEquipe("Equipe Listagem Teste", 3, "Pulverizacao");

        this.mockMvc.perform(MockMvcRequestBuilders.get(URL_EQUIPES))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].nome",
                        Matchers.hasItem("Equipe Listagem Teste")));
    }

    @Test
    void deveBuscarEquipePorIdERetornar200() throws Exception {
        Long id = this.criarEquipe("Equipe Busca Teste", 4, "Rocada mecanizada");

        this.mockMvc.perform(MockMvcRequestBuilders.get(URL_EQUIPES + "/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nome").value("Equipe Busca Teste"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantidadeIntegrantes").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$.especialidade").value("Rocada mecanizada"));
    }

    @Test
    void deveRetornar404AoBuscarEquipeInexistente() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(URL_EQUIPES + "/999999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Equipe de manutencao com ID 999999 nao encontrada."));
    }

    @Test
    void deveAtualizarEquipeERetornar200() throws Exception {
        Long id = this.criarEquipe("Equipe Atualizacao Teste", 2, "Pulverizacao");
        String json = """
                {
                  "nome": "Equipe Atualizada Teste",
                  "quantidadeIntegrantes": 6,
                  "especialidade": "Rocada mecanizada"
                }
                """;

        this.mockMvc.perform(MockMvcRequestBuilders.put(URL_EQUIPES + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nome").value("Equipe Atualizada Teste"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantidadeIntegrantes").value(6))
                .andExpect(MockMvcResultMatchers.jsonPath("$.especialidade").value("Rocada mecanizada"));
    }

    @Test
    void deveRetornar404AoAtualizarEquipeInexistente() throws Exception {
        String json = """
                {
                  "nome": "Equipe Inexistente Teste",
                  "quantidadeIntegrantes": 2,
                  "especialidade": "Pulverizacao"
                }
                """;

        this.mockMvc.perform(MockMvcRequestBuilders.put(URL_EQUIPES + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deveRemoverEquipeERetornar204() throws Exception {
        Long id = this.criarEquipe("Equipe Remocao Teste", 3, "Rocada manual");

        this.mockMvc.perform(MockMvcRequestBuilders.delete(URL_EQUIPES + "/" + id))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        this.mockMvc.perform(MockMvcRequestBuilders.get(URL_EQUIPES + "/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deveRetornar404AoRemoverEquipeInexistente() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete(URL_EQUIPES + "/999999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoDadosForemInvalidos() throws Exception {
        String json = """
                {
                  "nome": "",
                  "quantidadeIntegrantes": 0,
                  "especialidade": "Pulverizacao"
                }
                """;

        this.mockMvc.perform(MockMvcRequestBuilders.post(URL_EQUIPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Dados invalidos na requisicao."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detalhes", Matchers.hasItems(
                        "nome: O nome da equipe deve ser informado.",
                        "quantidadeIntegrantes: A equipe deve ter pelo menos um integrante.")));
    }

    @Test
    void deveRetornar409AoCadastrarEquipeComNomeDuplicado() throws Exception {
        this.criarEquipe("Equipe Duplicada Teste", 3, "Pulverizacao");
        String json = """
                {
                  "nome": "equipe duplicada teste",
                  "quantidadeIntegrantes": 5,
                  "especialidade": "Rocada manual"
                }
                """;

        this.mockMvc.perform(MockMvcRequestBuilders.post(URL_EQUIPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void deveFiltrarEquipesPorEspecialidade() throws Exception {
        this.criarEquipe("Equipe Filtro Alfa Teste", 3, "Especialidade Filtro Alfa");
        this.criarEquipe("Equipe Filtro Beta Teste", 4, "Especialidade Filtro Beta");

        this.mockMvc.perform(MockMvcRequestBuilders.get(URL_EQUIPES)
                        .param("especialidade", "filtro alfa"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nome").value("Equipe Filtro Alfa Teste"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].especialidade")
                        .value("Especialidade Filtro Alfa"));
    }

    private Long criarEquipe(String nome, int quantidadeIntegrantes, String especialidade) throws Exception {
        String json = """
                {
                  "nome": "%s",
                  "quantidadeIntegrantes": %d,
                  "especialidade": "%s"
                }
                """.formatted(nome, quantidadeIntegrantes, especialidade);

        MvcResult resultado = this.mockMvc.perform(MockMvcRequestBuilders.post(URL_EQUIPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        String localizacao = resultado.getResponse().getHeader("Location");
        return Long.valueOf(localizacao.substring(localizacao.lastIndexOf('/') + 1));
    }
}
