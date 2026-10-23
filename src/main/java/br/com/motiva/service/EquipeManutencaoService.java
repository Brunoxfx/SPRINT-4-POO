package br.com.motiva.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.motiva.dto.EquipeManutencaoRequest;
import br.com.motiva.exception.ConflitoException;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;

@Service
public class EquipeManutencaoService {
    private final EquipeManutencaoRepository equipeRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;

    public EquipeManutencaoService(EquipeManutencaoRepository equipeRepository,
            TrechoRodoviaRepository trechoRepository, IntervencaoOperacionalRepository intervencaoRepository) {
        this.equipeRepository = equipeRepository;
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipeManutencao> listarTodas() {
        return this.equipeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EquipeManutencao> listarPorEspecialidade(String especialidade) {
        return this.equipeRepository.findByEspecialidadeContainingIgnoreCase(especialidade.trim());
    }

    @Transactional(readOnly = true)
    public EquipeManutencao buscarPorId(Long id) {
        return this.equipeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Equipe de manutencao com ID " + id + " nao encontrada."));
    }

    @Transactional
    public EquipeManutencao cadastrar(EquipeManutencaoRequest request) {
        this.validar(request);
        String nome = request.nome().trim();
        if (this.equipeRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Ja existe uma equipe de manutencao com o nome " + nome + ".");
        }
        EquipeManutencao equipe = new EquipeManutencao(nome, request.quantidadeIntegrantes(),
                request.especialidade().trim());
        return this.equipeRepository.save(equipe);
    }

    @Transactional
    public EquipeManutencao atualizar(Long id, EquipeManutencaoRequest request) {
        EquipeManutencao equipe = this.buscarPorId(id);
        this.validar(request);
        String nome = request.nome().trim();
        if (this.equipeRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Ja existe uma equipe de manutencao com o nome " + nome + ".");
        }
        equipe.setNome(nome);
        equipe.setQuantidadeIntegrantes(request.quantidadeIntegrantes());
        equipe.setEspecialidade(request.especialidade().trim());
        return this.equipeRepository.save(equipe);
    }

    @Transactional
    public void remover(Long id) {
        EquipeManutencao equipe = this.buscarPorId(id);
        if (this.trechoRepository.existsByEquipeManutencaoId(id)) {
            throw new ConflitoException(
                    "A equipe de manutencao com ID " + id + " esta vinculada a trechos e nao pode ser removida.");
        }
        if (this.intervencaoRepository.existsByEquipeManutencaoId(id)) {
            throw new ConflitoException("A equipe de manutencao com ID " + id
                    + " esta vinculada a intervencoes e nao pode ser removida.");
        }
        this.equipeRepository.delete(equipe);
    }

    private void validar(EquipeManutencaoRequest request) {
        if (request.nome() == null || request.nome().trim().isEmpty()) {
            throw new RegraNegocioException("O nome da equipe deve ser informado.");
        }
        if (request.quantidadeIntegrantes() == null || request.quantidadeIntegrantes() <= 0) {
            throw new RegraNegocioException("A equipe deve ter pelo menos um integrante.");
        }
        if (request.especialidade() == null || request.especialidade().trim().isEmpty()) {
            throw new RegraNegocioException("A especialidade deve ser informada.");
        }
    }
}
