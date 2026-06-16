package br.com.exportador_relatorios.service.generator;

import br.com.exportador_relatorios.model.RelatorioJob;

import java.util.List;

public interface RelatorioGenerator {

    boolean suporta(String tipoRelatorio);

    List<String> gerarLinhas(RelatorioJob job);
}
