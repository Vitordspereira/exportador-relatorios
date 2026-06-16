package br.com.exportador_relatorios.dto;

import br.com.exportador_relatorios.enums.StatusRelatorio;

public record RelatorioStatusResponse(
        String idRelatorio,
        StatusRelatorio status,
        Integer processo,
        String mensagem,
        String downloadoURL
) {
}
