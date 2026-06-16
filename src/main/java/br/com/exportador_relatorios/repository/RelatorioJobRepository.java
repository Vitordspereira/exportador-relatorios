package br.com.exportador_relatorios.repository;

import br.com.exportador_relatorios.enums.StatusRelatorio;
import br.com.exportador_relatorios.model.RelatorioJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RelatorioJobRepository extends JpaRepository<RelatorioJob, String> {

    Optional<RelatorioJob> findByDownloadToken(String downloadToken);

    List<RelatorioJob> findByStatusRelatorioAndExpiraEmBefore(
            StatusRelatorio statusRelatorio,
            LocalDateTime dataHora
    );

    long countByStatusRelatorio(StatusRelatorio statusRelatorio);

    @Query("""
            SELECT r FROM RelatorioJob r
            WHERE (:status IS NULL OR r.statusRelatorio = :status)
            AND (:tipoRelatorio IS NULL OR LOWER(r.tipoRelatorio) LIKE LOWER(CONCAT('%', :tipoRelatorio, '%')))
            AND (:dataInicio IS NULL OR r.criadoEm >= :dataInicio)
            AND (:dataFim IS NULL OR r.criadoEm <= :dataFim)
            """)
    Page<RelatorioJob> buscarComFiltros(
            @Param("status") StatusRelatorio status,
            @Param("tipoRelatorio") String tipoRelatorio,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}
