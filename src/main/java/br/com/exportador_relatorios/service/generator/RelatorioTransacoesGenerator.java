package br.com.exportador_relatorios.service.generator;

import br.com.exportador_relatorios.model.RelatorioJob;
import br.com.exportador_relatorios.model.Transacao;
import br.com.exportador_relatorios.repository.TransacaoRepository;
import br.com.exportador_relatorios.util.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RelatorioTransacoesGenerator implements RelatorioGenerator {

    private final TransacaoRepository transacaoRepository;

    @Override
    public boolean suporta(String tipoRelatorio) {
        return "TRANSACOES".equals(tipoRelatorio);
    }

    @Override
    public List<String> gerarLinhas(RelatorioJob job) {
        List<String> linhas = new ArrayList<>();

        List<Transacao> transacoes = transacaoRepository.buscarPorPeriodo(
                job.getDataInicio(),
                job.getDataFim()
        );

        linhas.add(CsvUtils.linha("id", "descricao", "valor", "data"));

        for (Transacao transacao : transacoes) {
            linhas.add(CsvUtils.linha(
                    CsvUtils.texto(transacao.getIdTransacao()),
                    CsvUtils.texto(transacao.getDescricao()),
                    CsvUtils.dinheiro(transacao.getValor()),
                    CsvUtils.texto(transacao.getData())
            ));
        }

        return linhas;
    }
}
