package br.com.exportador_relatorios.service;

import br.com.exportador_relatorios.enums.StatusRelatorio;
import br.com.exportador_relatorios.model.RelatorioJob;
import br.com.exportador_relatorios.repository.RelatorioJobRepository;
import br.com.exportador_relatorios.service.generator.RelatorioGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ProcessadorRelatorioService {

    private final RelatorioJobRepository relatorioJobRepository;
    private final List<RelatorioGenerator> generators;

    @Async("relatorioTaskExecutor")
    public void processarEmBackground(String jobId) {
        RelatorioJob job = null;

        try {
            job = relatorioJobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Relatório não encontrado."));

            if (relatorioCancelado(jobId)) {
                return;
            }

            job.setStatusRelatorio(StatusRelatorio.PROCESSANDO);
            job.setProgresso(10);
            job.setMensagem("Iniciando processamento do relatório.");
            job.setIniciadoEm(LocalDateTime.now());
            job.setAtualizadoEm(LocalDateTime.now());
            relatorioJobRepository.save(job);

            pausar(100000);

            if (relatorioCancelado(jobId)) {
                return;
            }

            atualizarProgresso(job, 30, "Buscando dados para o relatório.");

            pausar(100000);

            if (relatorioCancelado(jobId)) {
                return;
            }

            atualizarProgresso(job, 60, "Montando arquivo CSV.");

            Path pastaRelatorios = Path.of("relatorios-gerados");
            Files.createDirectories(pastaRelatorios);

            String tipoArquivo = job.getTipoRelatorio() != null
                    ? job.getTipoRelatorio().toLowerCase()
                    : "generico";

            String nomeArquivo = "relatorio-" + tipoArquivo + "-" + job.getIdRelatorio() + ".csv";
            Path caminhoArquivo = pastaRelatorios.resolve(nomeArquivo);

            List<String> linhas = buscarGenerator(job.getTipoRelatorio()).gerarLinhas(job);

            List<String> linhasComBom = new ArrayList<>(linhas);

            if (!linhasComBom.isEmpty()) {
                linhasComBom.set(0, "\uFEFF" + linhasComBom.get(0));
            }

            Files.write(caminhoArquivo, linhasComBom, StandardCharsets.UTF_8);

            pausar(100000);

            if (relatorioCancelado(jobId)) {
                Files.deleteIfExists(caminhoArquivo);
                return;
            }

            atualizarProgresso(job, 90, "Finalizando relatório.");

            job.setStatusRelatorio(StatusRelatorio.CONCLUIDO);
            job.setProgresso(100);
            job.setMensagem("Relatório finalizado com sucesso");
            job.setArquivoNome(nomeArquivo);
            job.setArquivoPath(caminhoArquivo.toAbsolutePath().toString());
            job.setDownloadToken(UUID.randomUUID().toString());
            job.setExpiraEm(LocalDateTime.now().plusMinutes(5));
            job.setFinalizadoEm(LocalDateTime.now());
            job.setAtualizadoEm(LocalDateTime.now());

            relatorioJobRepository.save(job);

        } catch (Exception e) {
            if (job != null && job.getStatusRelatorio() != StatusRelatorio.CANCELADO) {
                job.setStatusRelatorio(StatusRelatorio.ERRO);
                job.setProgresso(0);
                job.setMensagem("Erro ao gerar relatório");
                job.setErro(e.getMessage());
                job.setAtualizadoEm(LocalDateTime.now());
                relatorioJobRepository.save(job);
            }
        }
    }

    private RelatorioGenerator buscarGenerator(String tipoRelatorio) {
        return generators.stream()
                .filter(generator -> generator.suporta(tipoRelatorio))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tipo de relatório não suportado: " + tipoRelatorio));
    }

    private void atualizarProgresso(RelatorioJob job, int progresso, String mensagem) {
        job.setProgresso(progresso);
        job.setMensagem(mensagem);
        job.setAtualizadoEm(LocalDateTime.now());
        relatorioJobRepository.save(job);
    }

    private void pausar(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processamento interrompido.");
        }
    }

    private boolean relatorioCancelado(String jobId) {
        return relatorioJobRepository.findById(jobId)
                .map(job -> job.getStatusRelatorio() == StatusRelatorio.CANCELADO)
                .orElse(true);
    }
}