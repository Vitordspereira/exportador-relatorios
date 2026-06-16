package br.com.exportador_relatorios.controller;

import br.com.exportador_relatorios.dto.RelatorioRequest;
import br.com.exportador_relatorios.dto.RelatorioResumoDashboardResponse;
import br.com.exportador_relatorios.dto.RelatorioResumoResponse;
import br.com.exportador_relatorios.dto.RelatorioStatusResponse;
import br.com.exportador_relatorios.enums.StatusRelatorio;
import br.com.exportador_relatorios.service.RelatorioService;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @PostMapping("/exportar")
    public ResponseEntity<RelatorioStatusResponse> exportar(@RequestBody @Valid RelatorioRequest relatorioRequest) {

        RelatorioStatusResponse relatorioStatusResponse = relatorioService.solicitarRelatorio(relatorioRequest);
        return ResponseEntity.ok(relatorioStatusResponse);
    }

    @GetMapping("/{idRelatorio}/status")
    public ResponseEntity<RelatorioStatusResponse> consultarStatus(@PathVariable String idRelatorio) {

        RelatorioStatusResponse relatorioStatusResponse = relatorioService.consultarStatus(idRelatorio);
        return ResponseEntity.ok(relatorioStatusResponse);
    }

    @PostMapping("/{idRelatorio}/reprocessar")
    public ResponseEntity<RelatorioStatusResponse> reprocessarRelatorio(
            @PathVariable String idRelatorio
    ) {
        RelatorioStatusResponse response = relatorioService.reprocessarRelatorio(idRelatorio);
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/{idRelatorio}/cancelar")
    public ResponseEntity<RelatorioStatusResponse> cancelarRelatorio(
            @PathVariable String idRelatorio
    ) {
        RelatorioStatusResponse relatorioStatusResponse = relatorioService.cancelarRelatorio(idRelatorio);
        return ResponseEntity.ok(relatorioStatusResponse);
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> baixarRelatorio(@PathVariable String token) {
        return relatorioService.baixarRelatorio(token);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<String>> listarTiposRelatorio() {
        List<String> tipos = relatorioService.listarTiposRelatorio();
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/resumo")
    public ResponseEntity<RelatorioResumoDashboardResponse> obterResumoRelatorios() {
        RelatorioResumoDashboardResponse resumo = relatorioService.obterResumoRelatorios();
        return ResponseEntity.ok(resumo);
    }

    @GetMapping
    public ResponseEntity<Page<RelatorioResumoResponse>> listarRelatorios(
            @RequestParam(required = false) StatusRelatorio status,

            @RequestParam(required = false) String tipoRelatorio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFim,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RelatorioResumoResponse> relatorios =
                relatorioService.listarRelatoriosComFiltros(
                        status,
                        tipoRelatorio,
                        dataInicio,
                        dataFim,
                        page,
                        size
                );

        return ResponseEntity.ok(relatorios);
    }

    @DeleteMapping("/{idRelatorio}")
    public ResponseEntity<Void> excluirRelatorio(
            @PathVariable String idRelatorio
    ){
        relatorioService.excluirRelatorio(idRelatorio);
        return ResponseEntity.noContent().build();
    }
}
