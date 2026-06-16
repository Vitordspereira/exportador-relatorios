package br.com.exportador_relatorios.service;

import br.com.exportador_relatorios.dto.RelatorioRequest;
import br.com.exportador_relatorios.dto.RelatorioResumoDashboardResponse;
import br.com.exportador_relatorios.dto.RelatorioResumoResponse;
import br.com.exportador_relatorios.dto.RelatorioStatusResponse;
import br.com.exportador_relatorios.enums.StatusRelatorio;
import br.com.exportador_relatorios.enums.TipoRelatorio;
import br.com.exportador_relatorios.model.RelatorioJob;
import br.com.exportador_relatorios.repository.RelatorioJobRepository;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final RelatorioJobRepository relatorioJobRepository;
    private final ProcessadorRelatorioService processadorRelatorioService;

    public RelatorioStatusResponse solicitarRelatorio(RelatorioRequest relatorioRequest) {
        LocalDateTime agora = LocalDateTime.now();

        RelatorioJob job = new RelatorioJob();
        job.setIdRelatorio(UUID.randomUUID().toString());
        job.setTipoRelatorio(relatorioRequest.tipoRelatorio().name());
        job.setDataInicio(relatorioRequest.dataInicio());
        job.setDataFim(relatorioRequest.dataFim());
        job.setStatusRelatorio(StatusRelatorio.PENDENTE);
        job.setProgresso(0);
        job.setMensagem("Relatório aguardando processamento");
        job.setCriadoEm(agora);
        job.setAtualizadoEm(agora);

        relatorioJobRepository.save(job);

        processadorRelatorioService.processarEmBackground(job.getIdRelatorio());

        return montarResponse(job);
    }

    public RelatorioStatusResponse consultarStatus(String id) {
        RelatorioJob job = relatorioJobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Relatório não encontrado."
                ));

        return montarResponse(job);
    }

    private RelatorioStatusResponse montarResponse(RelatorioJob job) {
        String downloadURL = null;

        if (job.getStatusRelatorio() == StatusRelatorio.CONCLUIDO && job.getDownloadToken() != null) {
            downloadURL = "/relatorios/download/" + job.getDownloadToken();
        }

        return new RelatorioStatusResponse(
                job.getIdRelatorio(),
                job.getStatusRelatorio(),
                job.getProgresso(),
                job.getMensagem(),
                downloadURL
        );
    }

    private RelatorioResumoResponse montarResumoResponse(RelatorioJob relatorioJob) {
        String downloadURL = null;

        boolean linkDisponivel = relatorioJob.getStatusRelatorio() == StatusRelatorio.CONCLUIDO
                && relatorioJob.getDownloadToken() !=null
                && relatorioJob.getExpiraEm() !=null
                && relatorioJob.getExpiraEm().isAfter(LocalDateTime.now());

        if (linkDisponivel) {
            downloadURL = "/relatorios/download/" + relatorioJob.getDownloadToken();
        }

        return new RelatorioResumoResponse(
                relatorioJob.getIdRelatorio(),
                relatorioJob.getTipoRelatorio(),
                relatorioJob.getStatusRelatorio(),
                relatorioJob.getProgresso(),
                relatorioJob.getMensagem(),
                downloadURL,
                relatorioJob.getCriadoEm(),
                relatorioJob.getAtualizadoEm(),
                relatorioJob.getExpiraEm(),
                relatorioJob.getDataInicio(),
                relatorioJob.getDataFim()
        );
    }

    public ResponseEntity<Resource> baixarRelatorio(String token) {
        RelatorioJob relatorioJob = relatorioJobRepository.findByDownloadToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Link de download não encontrado."
                ));

        if (relatorioJob.getStatusRelatorio() == StatusRelatorio.EXPIRADO) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Link de download expirado."
            );
        }

        if (relatorioJob.getStatusRelatorio() != StatusRelatorio.CONCLUIDO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Relatório ainda não está disponível para download."
            );
        }

        if (relatorioJob.getExpiraEm() == null || relatorioJob.getExpiraEm().isBefore(LocalDateTime.now())) {
            relatorioJob.setStatusRelatorio(StatusRelatorio.EXPIRADO);
            relatorioJob.setMensagem("Link de download expirado.");
            relatorioJobRepository.save(relatorioJob);

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Link de download expirado."
            );
        }

        if (relatorioJob.getArquivoPath() == null || relatorioJob.getArquivoPath().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Arquivo de relatório não encontrado."
            );
        }

        try {
            Path caminhoArquivo = Path.of(relatorioJob.getArquivoPath());

            if (!Files.exists(caminhoArquivo)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Arquivo físico não encontrado no servidor."
                );
            }

            Resource resource = new UrlResource(caminhoArquivo.toUri());

            String nomeArquivo = relatorioJob.getArquivoNome() != null
                    ?relatorioJob.getArquivoNome()
                    :"relatorio.csv";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nomeArquivo + "\""
                    )
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao preparar o arquivo para download."
            );
        }
    }

    public Page<RelatorioResumoResponse> listarRelatoriosComFiltros(
            StatusRelatorio statusRelatorio,
            String tipoRelatorio,
            LocalDate dataInicio,
            LocalDate dataFim,
            int page,
            int size
    ) {
        if (size > 50) {
            size = 50;
        }

        if (size <= 0) {
            size = 10;
        }

        if (page < 0) {
            page = 0;
        }

        LocalDateTime dataInicioDateTime = dataInicio != null
                ? dataInicio.atStartOfDay()
                : null;

        LocalDateTime dataFimDateTime = dataFim != null
                ? dataFim.atTime(23, 59, 59)
                : null;

        String tipoTratado = tipoRelatorio != null && !tipoRelatorio.isBlank()
                ? tipoRelatorio.trim()
                : null;

        Pageable pageable= PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "criadoEm")
        );

        return relatorioJobRepository.buscarComFiltros(
                statusRelatorio,
                tipoTratado,
                dataInicioDateTime,
                dataFimDateTime,
                pageable
        ).map(this::montarResumoResponse);
    }

    public List<String> listarTiposRelatorio() {
        return Arrays.stream(TipoRelatorio.values())
                .map(Enum::name)
                .toList();
    }

    public RelatorioResumoDashboardResponse obterResumoRelatorios() {
        long total = relatorioJobRepository.count();

        long pendentes = relatorioJobRepository.countByStatusRelatorio(StatusRelatorio.PENDENTE);
        long processando = relatorioJobRepository.countByStatusRelatorio(StatusRelatorio.PROCESSANDO);
        long concluidos = relatorioJobRepository.countByStatusRelatorio(StatusRelatorio.CONCLUIDO);
        long erros = relatorioJobRepository.countByStatusRelatorio(StatusRelatorio.ERRO);
        long expirados = relatorioJobRepository.countByStatusRelatorio(StatusRelatorio.EXPIRADO);

        return new RelatorioResumoDashboardResponse(
                total,
                pendentes,
                processando,
                concluidos,
                erros,
                expirados
        );
    }

    public RelatorioStatusResponse reprocessarRelatorio(String idRelatorio) {
        RelatorioJob relatorioJob = relatorioJobRepository.findById(idRelatorio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Relatório não encontrado."
                ));

        if (relatorioJob.getStatusRelatorio() == StatusRelatorio.PROCESSANDO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível reprocessar um relatórioque já está em processamento."
            );
        }

        apagarArquivoAntigo(relatorioJob);

        LocalDateTime agora = LocalDateTime.now();

        relatorioJob.setStatusRelatorio(StatusRelatorio.PENDENTE);
        relatorioJob.setProgresso(0);
        relatorioJob.setMensagem("Relatório enviado para processamento");

        relatorioJob.setArquivoNome(null);
        relatorioJob.setArquivoPath(null);
        relatorioJob.setDownloadToken(null);
        relatorioJob.setExpiraEm(null);
        relatorioJob.setErro(null);

        relatorioJob.setIniciadoEm(null);
        relatorioJob.setFinalizadoEm(null);
        relatorioJob.setAtualizadoEm(agora);

        relatorioJobRepository.save(relatorioJob);

        processadorRelatorioService.processarEmBackground(relatorioJob.getIdRelatorio());

        return montarResponse(relatorioJob);
    }

    private void apagarArquivoAntigo(RelatorioJob relatorioJob) {
        try {
            if (relatorioJob.getArquivoPath() == null || relatorioJob.getArquivoPath().isBlank()) {
                return;
            }

            Path caminhoArquivo = Path.of(relatorioJob.getArquivoPath());

            Files.deleteIfExists(caminhoArquivo);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao apagar arquivo antigo do relatório."
            );
        }
    }

    public RelatorioStatusResponse cancelarRelatorio(String idRelatorio) {
        RelatorioJob relatorioJob = relatorioJobRepository.findById(idRelatorio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado."));

        if (relatorioJob.getStatusRelatorio() == StatusRelatorio.CONCLUIDO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível cancelar um relatório já concluído."
            );
        }

        if (relatorioJob.getStatusRelatorio() == StatusRelatorio.EXPIRADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível cancelar um relatório já expirado."
            );
        }

        if (relatorioJob.getStatusRelatorio() == StatusRelatorio.CANCELADO) {
            return montarResponse(relatorioJob);
        }

        relatorioJob.setStatusRelatorio(StatusRelatorio.CANCELADO);
        relatorioJob.setMensagem("Relatório cancelado pelo usuário.");
        relatorioJob.setAtualizadoEm(LocalDateTime.now());

        relatorioJobRepository.save(relatorioJob);

        return montarResponse(relatorioJob);
    }

    public void excluirRelatorio(String idRelatorio) {
        RelatorioJob relatorioJob = relatorioJobRepository.findById(idRelatorio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Relatório não encontrado."
                ));

        if (relatorioJob.getStatusRelatorio() == StatusRelatorio.PROCESSANDO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível excluir um relatório em processamento."
            );
        }

        apagarArquivoAntigo(relatorioJob);

        relatorioJobRepository.delete(relatorioJob);
    }
}
