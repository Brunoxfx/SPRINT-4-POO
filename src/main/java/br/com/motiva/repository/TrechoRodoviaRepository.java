package br.com.motiva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.motiva.model.TrechoRodovia;

public interface TrechoRodoviaRepository extends JpaRepository<TrechoRodovia, Long> {
    List<TrechoRodovia> findByAlturaVegetacaoCmGreaterThanEqual(Double minimo);

    List<TrechoRodovia> findByRiscoOperacional(String riscoOperacional);

    List<TrechoRodovia> findByTipoAmbiente(String tipoAmbiente);

    List<TrechoRodovia> findByEquipeManutencaoId(Long equipeId);

    boolean existsByEquipeManutencaoId(Long equipeId);

    boolean existsByKmInicialAndKmFinal(Double kmInicial, Double kmFinal);

    boolean existsByKmInicialAndKmFinalAndIdNot(Double kmInicial, Double kmFinal, Long id);
}
