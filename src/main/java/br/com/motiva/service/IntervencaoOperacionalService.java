package br.com.motiva.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.motiva.dto.IntervencaoOperacionalRequest;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;

@Service
public class IntervencaoOperacionalService {
    private final IntervencaoOperacionalRepository intervencaoRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final EquipeManutencaoRepository equipeRepository;

    public IntervencaoOperacionalService(IntervencaoOperacionalRepository intervencaoRepository,
            TrechoRodoviaRepository trechoRepository, EquipeManutencaoRepository equipeRepository) {
        this.intervencaoRepository = intervencaoRepository;
        this.trechoRepository = trechoRepository;
        this.equipeRepository = equipeRepository;
    }

    @Transactional(readOnly = true)
    public List<IntervencaoOperacional> listarTodas() {
        return this.intervencaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<IntervencaoOperacional> listarPorTipo(String tipo) {
        return this.intervencaoRepository.findByTipo(this.normalizarTipo(tipo));
    }

    @Transactional(readOnly = true)
    public List<IntervencaoOperacional> listarPorTrecho(Long trechoId) {
        return this.intervencaoRepository.findByTrechoId(trechoId);
    }

    @Transactional(readOnly = true)
    public List<IntervencaoOperacional> listarPorEquipe(Long equipeId) {
        return this.intervencaoRepository.findByEquipeManutencaoId(equipeId);
    }

    @Transactional(readOnly = true)
    public IntervencaoOperacional buscarPorId(Long id) {
        return this.intervencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Intervencao operacional com ID " + id + " nao encontrada."));
    }

    @Transactional
    public IntervencaoOperacional cadastrar(IntervencaoOperacionalRequest requisicao) {
        String tipo = this.normalizarTipo(requisicao.tipo());
        TrechoRodovia trecho = this.buscarTrecho(requisicao.trechoId());
        EquipeManutencao equipe = this.buscarEquipeOpcional(requisicao.equipeId());
        IntervencaoOperacional intervencao = new IntervencaoOperacional(tipo, trecho, equipe);
        return this.intervencaoRepository.save(intervencao);
    }

    @Transactional
    public IntervencaoOperacional atualizar(Long id, IntervencaoOperacionalRequest requisicao) {
        IntervencaoOperacional intervencao = this.buscarPorId(id);
        String tipo = this.normalizarTipo(requisicao.tipo());
        TrechoRodovia trecho = this.buscarTrecho(requisicao.trechoId());
        EquipeManutencao equipe = this.buscarEquipeOpcional(requisicao.equipeId());
        intervencao.setTipo(tipo);
        intervencao.setTrecho(trecho);
        intervencao.setEquipeManutencao(equipe);
        return this.intervencaoRepository.save(intervencao);
    }

    @Transactional
    public void remover(Long id) {
        IntervencaoOperacional intervencao = this.buscarPorId(id);
        this.intervencaoRepository.delete(intervencao);
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new RegraNegocioException("O tipo da intervencao deve ser informado.");
        }
        String tipoNormalizado = tipo.trim().toUpperCase(Locale.ROOT);
        if (!this.isTipoValido(tipoNormalizado)) {
            throw new RegraNegocioException(
                    "O tipo da intervencao deve ser ROCADA_MANUAL, ROCADA_MECANIZADA ou PULVERIZACAO.");
        }
        return tipoNormalizado;
    }

    private boolean isTipoValido(String tipo) {
        return IntervencaoOperacional.TIPO_ROCADA_MANUAL.equals(tipo)
                || IntervencaoOperacional.TIPO_ROCADA_MECANIZADA.equals(tipo)
                || IntervencaoOperacional.TIPO_PULVERIZACAO.equals(tipo);
    }

    private TrechoRodovia buscarTrecho(Long trechoId) {
        if (trechoId == null) {
            throw new RegraNegocioException("O ID do trecho deve ser informado.");
        }
        return this.trechoRepository.findById(trechoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Trecho de rodovia com ID " + trechoId + " nao encontrado."));
    }

    private EquipeManutencao buscarEquipeOpcional(Long equipeId) {
        if (equipeId == null) {
            return null;
        }
        return this.equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Equipe de manutencao com ID " + equipeId + " nao encontrada."));
    }
}
