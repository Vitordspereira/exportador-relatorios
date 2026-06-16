package br.com.exportador_relatorios.service.generator;

import br.com.exportador_relatorios.model.Cliente;
import br.com.exportador_relatorios.model.RelatorioJob;
import br.com.exportador_relatorios.repository.ClienteRepository;
import br.com.exportador_relatorios.util.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.event.CaretListener;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RelatorioClienteGenerator implements RelatorioGenerator{

    private final ClienteRepository clienteRepository;

    @Override
    public boolean suporta(String tipoRelatorio) {
        return "CLIENTES".equals(tipoRelatorio);
    }

    @Override
    public List<String> gerarLinhas(RelatorioJob job) {
        List<String> linhas = new ArrayList<>();

        List<Cliente> clientes = clienteRepository.buscarPorPeriodo(
                job.getDataInicio(),
                job.getDataFim()
        );

        linhas.add(CsvUtils.linha("id", "nome", "email", "telefone", "data_cadastro"));

        for (Cliente cliente : clientes) {
            linhas.add(CsvUtils.linha(
                    CsvUtils.texto(cliente.getIdCliente()),
                    CsvUtils.texto(cliente.getNome()),
                    CsvUtils.texto(cliente.getEmail()),
                    CsvUtils.texto(cliente.getTelefone()),
                    CsvUtils.texto(cliente.getDataCadastro())
            ));
        }

        return linhas;
    }
}
