package br.com.motiva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.TrechoRodovia;

class MotorPrioridadeTest {
    private final MotorPrioridade motor = new MotorPrioridade();

    @Test
    void deveClassificarComoUrgenteVegetacaoAltaComRiscoAlto() {
        this.verificar(92.0, TrechoRodovia.RISCO_ALTO, MotorPrioridade.PRIORIDADE_URGENTE,
                IntervencaoOperacional.TIPO_ROCADA_MANUAL);
    }

    @Test
    void deveClassificarComoUrgenteNoLimiteDe80ComRiscoAlto() {
        this.verificar(80.0, TrechoRodovia.RISCO_ALTO, MotorPrioridade.PRIORIDADE_URGENTE,
                IntervencaoOperacional.TIPO_ROCADA_MANUAL);
    }

    @Test
    void deveClassificarComoCriticoVegetacaoAltaComRiscoBaixo() {
        this.verificar(85.0, TrechoRodovia.RISCO_BAIXO, MotorPrioridade.PRIORIDADE_CRITICO,
                IntervencaoOperacional.TIPO_ROCADA_MECANIZADA);
    }

    @Test
    void deveClassificarComoCriticoNoLimiteDe80ComRiscoBaixo() {
        this.verificar(80.0, TrechoRodovia.RISCO_BAIXO, MotorPrioridade.PRIORIDADE_CRITICO,
                IntervencaoOperacional.TIPO_ROCADA_MECANIZADA);
    }

    @Test
    void deveClassificarComoAtencaoLogoAbaixoDe80MesmoComRiscoAlto() {
        this.verificar(79.9, TrechoRodovia.RISCO_ALTO, MotorPrioridade.PRIORIDADE_ATENCAO,
                IntervencaoOperacional.TIPO_PULVERIZACAO);
    }

    @Test
    void deveClassificarComoAtencaoNoLimiteDe40() {
        this.verificar(40.0, TrechoRodovia.RISCO_BAIXO, MotorPrioridade.PRIORIDADE_ATENCAO,
                IntervencaoOperacional.TIPO_PULVERIZACAO);
    }

    @Test
    void deveClassificarComoNormalLogoAbaixoDe40MesmoComRiscoAlto() {
        this.verificar(39.9, TrechoRodovia.RISCO_ALTO, MotorPrioridade.PRIORIDADE_NORMAL, null);
    }

    @Test
    void deveClassificarComoNormalSemVegetacao() {
        this.verificar(0.0, TrechoRodovia.RISCO_BAIXO, MotorPrioridade.PRIORIDADE_NORMAL, null);
    }

    @Test
    void deveManterNomeEMotivoDaSprint3() {
        ItemRelatorioPrioridade urgente = this.motor.avaliarTrecho(this.criarTrecho(92.0, TrechoRodovia.RISCO_ALTO));
        ItemRelatorioPrioridade normal = this.motor.avaliarTrecho(this.criarTrecho(10.0, TrechoRodovia.RISCO_BAIXO));

        assertEquals("Rocada manual", urgente.nomeIntervencao());
        assertEquals("Vegetacao alta em area com risco operacional alto.", urgente.motivo());
        assertTrue(urgente.precisaIntervencao());
        assertNull(normal.nomeIntervencao());
        assertEquals("Vegetacao baixa. Nao ha prioridade de servico.", normal.motivo());
        assertFalse(normal.precisaIntervencao());
    }

    @Test
    void deveCalcularResumoNoFormatoDaSprint3() {
        List<TrechoRodovia> trechos = new ArrayList<TrechoRodovia>();
        trechos.add(this.criarTrecho(92.0, TrechoRodovia.RISCO_ALTO));
        trechos.add(this.criarTrecho(85.0, TrechoRodovia.RISCO_BAIXO));
        trechos.add(this.criarTrecho(81.0, TrechoRodovia.RISCO_BAIXO));
        trechos.add(this.criarTrecho(50.0, TrechoRodovia.RISCO_ALTO));
        trechos.add(this.criarTrecho(10.0, TrechoRodovia.RISCO_BAIXO));

        List<ItemRelatorioPrioridade> itens = this.motor.gerarRelatorio(trechos);
        GeradorRelatorio gerador = new GeradorRelatorio(this.motor, null, null);
        ResumoPrioridades resumo = gerador.calcularResumo(itens);

        assertEquals(5, itens.size());
        assertEquals(1, resumo.quantidadeUrgente());
        assertEquals(2, resumo.quantidadeCritico());
        assertEquals(1, resumo.quantidadeAtencao());
        assertEquals(1, resumo.quantidadeNormal());
        assertEquals("Urgente: 1, Critico: 2, Atencao: 1, Normal: 1", resumo.texto());
    }

    @Test
    void deveCalcularResumoZeradoSemTrechos() {
        GeradorRelatorio gerador = new GeradorRelatorio(this.motor, null, null);
        ResumoPrioridades resumo = gerador.calcularResumo(new ArrayList<ItemRelatorioPrioridade>());

        assertEquals("Urgente: 0, Critico: 0, Atencao: 0, Normal: 0", resumo.texto());
    }

    private void verificar(double altura, String risco, String prioridadeEsperada, String tipoEsperado) {
        ItemRelatorioPrioridade item = this.motor.avaliarTrecho(this.criarTrecho(altura, risco));

        assertEquals(prioridadeEsperada, item.prioridade());
        assertEquals(tipoEsperado, item.tipoIntervencao());
    }

    private TrechoRodovia criarTrecho(double altura, String risco) {
        return new TrechoRodovia(10.0, 12.0, altura, TrechoRodovia.AMBIENTE_SECO, risco, null);
    }
}
