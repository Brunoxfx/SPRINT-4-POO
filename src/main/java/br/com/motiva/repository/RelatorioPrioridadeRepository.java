package br.com.motiva.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.motiva.model.RelatorioPrioridade;

public interface RelatorioPrioridadeRepository extends JpaRepository<RelatorioPrioridade, Long> {
    List<RelatorioPrioridade> findAllByOrderByDataGeracaoDesc();

    List<RelatorioPrioridade> findByDataGeracaoBetweenOrderByDataGeracaoAsc(LocalDateTime inicio,
            LocalDateTime fim);
}
