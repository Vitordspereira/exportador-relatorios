package br.com.exportador_relatorios.repository;

import br.com.exportador_relatorios.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("""
            SELECT t FROM Transacao t
            WHERE (:dataInicio IS NULL OR t.data >= :dataInicio)
            AND (:dataFim IS NULL OR t.data <= :dataFim)
            ORDER BY t.data ASC
            """)
    List<Transacao> buscarPorPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
