package br.com.exportador_relatorios.dto;

public record RelatorioResumoDashboardResponse(
        long total,
        long pendentes,
        long processando,
        long concluidos,
        long erros,
        long expirados
) {
}
