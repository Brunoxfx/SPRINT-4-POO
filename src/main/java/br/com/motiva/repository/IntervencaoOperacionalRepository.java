package br.com.motiva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.motiva.model.IntervencaoOperacional;

public interface IntervencaoOperacionalRepository extends JpaRepository<IntervencaoOperacional, Long> {
    List<IntervencaoOperacional> findByTipo(String tipo);

    List<IntervencaoOperacional> findByTrechoId(Long trechoId);

    List<IntervencaoOperacional> findByEquipeManutencaoId(Long equipeId);

    boolean existsByTrechoId(Long trechoId);

    boolean existsByEquipeManutencaoId(Long equipeId);
}
