package br.com.exportador_relatorios.service;

import br.com.exportador_relatorios.enums.StatusRelatorio;
import br.com.exportador_relatorios.model.RelatorioJob;
import br.com.exportador_relatorios.repository.RelatorioJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LimpezaRelatorioService {

    private final RelatorioJobRepository relatorioJobRepository;

    @Scheduled(fixedRate = 60000)
    public void limparRelatoriosExpirados() {
        LocalDateTime agora = LocalDateTime.now();

        List<RelatorioJob> relatoriosExpirados = relatorioJobRepository.findByStatusRelatorioAndExpiraEmBefore(
                StatusRelatorio.CONCLUIDO,
                agora
        );

        for (RelatorioJob relatorioJob : relatoriosExpirados) {
            apagarArquivo(relatorioJob);

            relatorioJob.setStatusRelatorio(StatusRelatorio.EXPIRADO);
            relatorioJob.setMensagem("Relatório expirado e arquivo removido.");
            relatorioJob.setAtualizadoEm(LocalDateTime.now());

            relatorioJobRepository.save(relatorioJob);
        }
    }

    private void apagarArquivo(RelatorioJob relatorioJob) {
        try {
            if (relatorioJob.getArquivoPath() == null || relatorioJob.getArquivoPath().isBlank()) {
                return;
            }

            Path caminhoArquivo = Path.of(relatorioJob.getArquivoPath());

            Files.deleteIfExists(caminhoArquivo);
        } catch (Exception e) {
            System.out.println("Erro ao apagar arquivo do relatório "
                    + relatorioJob.getIdRelatorio()
                    + ": "
                    +e.getMessage());
        }
    }
}
