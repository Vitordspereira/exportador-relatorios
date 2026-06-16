package br.com.exportador_relatorios.dto;

import br.com.exportador_relatorios.enums.TipoRelatorio;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RelatorioRequest(

        @NotNull(message = "O tipo de relatório é obrigatório")
        TipoRelatorio tipoRelatorio,

        LocalDate dataInicio,

        LocalDate dataFim
) {
}
