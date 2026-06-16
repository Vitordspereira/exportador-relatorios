package br.com.exportador_relatorios.repository;

import br.com.exportador_relatorios.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("""
            SELECT c FROM Cliente c
            WHERE (:dataInicio IS NULL OR c.dataCadastro >= :dataInicio)
            AND (:dataFim IS NULL OR c.dataCadastro <= :dataFim)
            ORDER BY c.dataCadastro ASC
            """)
    List<Cliente> buscarPorPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
