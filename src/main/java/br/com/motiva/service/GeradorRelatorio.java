package br.com.motiva.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.RelatorioPrioridade;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.RelatorioPrioridadeRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;

@Service
public class GeradorRelatorio {
    private final MotorPrioridade motor;
    private final TrechoRodoviaRepository trechoRepository;
    private final RelatorioPrioridadeRepository relatorioRepository;

    public GeradorRelatorio(MotorPrioridade motor, TrechoRodoviaRepository trechoRepository,
            RelatorioPrioridadeRepository relatorioRepository) {
        this.motor = motor;
        this.trechoRepository = trechoRepository;
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional
    public RelatorioGerado gerarRelatorio() {
        List<TrechoRodovia> trechos = this.trechoRepository.findAll();
        List<ItemRelatorioPrioridade> itens = this.motor.gerarRelatorio(trechos);
        ResumoPrioridades resumo = this.calcularResumo(itens);

        RelatorioPrioridade relatorio = new RelatorioPrioridade(resumo.quantidadeUrgente(),
                resumo.quantidadeCritico(), resumo.quantidadeAtencao(), resumo.quantidadeNormal(), resumo.texto(),
                LocalDateTime.now());
        RelatorioPrioridade salvo = this.relatorioRepository.save(relatorio);

        return new RelatorioGerado(salvo, itens);
    }

    @Transactional(readOnly = true)
    public List<RelatorioPrioridade> listarHistorico() {
        return this.relatorioRepository.findAllByOrderByDataGeracaoDesc();
    }

    @Transactional(readOnly = true)
    public RelatorioPrioridade buscarPorId(Long id) {
        return this.relatorioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(
                "Relatorio de prioridade com ID " + id + " nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<RelatorioPrioridade> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("As datas de inicio e fim devem ser informadas.");
        }
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data de inicio deve ser anterior ou igual a data de fim.");
        }

        return this.relatorioRepository.findByDataGeracaoBetweenOrderByDataGeracaoAsc(inicio.atStartOfDay(),
                fim.atTime(LocalTime.MAX));
    }

    public ResumoPrioridades calcularResumo(List<ItemRelatorioPrioridade> itens) {
        int quantidadeUrgente = 0;
        int quantidadeCritico = 0;
        int quantidadeAtencao = 0;
        int quantidadeNormal = 0;

        for (ItemRelatorioPrioridade item : itens) {
            String prioridade = item.prioridade();

            if (MotorPrioridade.PRIORIDADE_URGENTE.equals(prioridade)) {
                quantidadeUrgente++;
            } else if (MotorPrioridade.PRIORIDADE_CRITICO.equals(prioridade)) {
                quantidadeCritico++;
            } else if (MotorPrioridade.PRIORIDADE_ATENCAO.equals(prioridade)) {
                quantidadeAtencao++;
            } else {
                quantidadeNormal++;
            }
        }

        String texto = "Urgente: " + quantidadeUrgente
                + ", Critico: " + quantidadeCritico
                + ", Atencao: " + quantidadeAtencao
                + ", Normal: " + quantidadeNormal;

        return new ResumoPrioridades(quantidadeUrgente, quantidadeCritico, quantidadeAtencao, quantidadeNormal,
                texto);
    }
}
