package br.com.exportador_relatorios.dto;

import br.com.exportador_relatorios.enums.StatusRelatorio;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RelatorioResumoResponse(
        String idRelatorio,
        String tipoRelatorio,
        StatusRelatorio statusRelatorio,
        Integer progresso,
        String mensagem,
        String downloadURL,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime expiraEm,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
