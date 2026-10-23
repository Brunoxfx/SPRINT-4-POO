package br.com.motiva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.motiva.model.EquipeManutencao;

public interface EquipeManutencaoRepository extends JpaRepository<EquipeManutencao, Long> {
    List<EquipeManutencao> findByEspecialidadeContainingIgnoreCase(String especialidade);

    List<EquipeManutencao> findByQuantidadeIntegrantesGreaterThanEqual(Integer minimo);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
