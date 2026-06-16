package br.com.exportador_relatorios.repository;

import br.com.exportador_relatorios.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("""
            SELECT v FROM Venda v
            WHERE (:dataInicio IS NULL OR v.data >= :dataInicio)
            AND (:dataFim IS NULL OR v.data <= :dataFim)
            ORDER BY v.data ASC
            """)
    List<Venda> buscarPorPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
