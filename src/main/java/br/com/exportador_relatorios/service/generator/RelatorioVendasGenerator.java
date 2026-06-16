package br.com.exportador_relatorios.service.generator;

import br.com.exportador_relatorios.model.RelatorioJob;
import br.com.exportador_relatorios.model.Venda;
import br.com.exportador_relatorios.repository.VendaRepository;
import br.com.exportador_relatorios.util.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RelatorioVendasGenerator implements RelatorioGenerator {

    private final VendaRepository vendaRepository;

    @Override
    public boolean suporta(String tipoRelatorio) {
        return "VENDAS".equals(tipoRelatorio);
    }

    @Override
    public List<String> gerarLinhas(RelatorioJob job) {
        List<String> linhas = new ArrayList<>();

        List<Venda> vendas = vendaRepository.buscarPorPeriodo(
                job.getDataInicio(),
                job.getDataFim()
        );

        linhas.add(CsvUtils.linha("id", "produto", "quantidade", "total", "data"));

        for (Venda venda : vendas) {
            linhas.add(CsvUtils.linha(
                    CsvUtils.texto(venda.getIdVenda()),
                    CsvUtils.texto(venda.getProduto()),
                    CsvUtils.texto(venda.getQuantidade()),
                    CsvUtils.dinheiro(venda.getTotal()),
                    CsvUtils.texto(venda.getData())
            ));
        }

        return linhas;
    }
}