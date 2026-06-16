package br.com.exportador_relatorios.model;

import br.com.exportador_relatorios.enums.StatusRelatorio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "relatorio_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioJob {

    @Id
    private String idRelatorio;

    @Column(name = "tipo_relatorio", nullable = false)
    private String tipoRelatorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRelatorio statusRelatorio;

    @Column(nullable = false)
    private Integer progresso;

    private String mensagem;

    private String arquivoNome;

    private String arquivoPath;

    private String downloadToken;

    private LocalDateTime expiraEm;

    @Column(columnDefinition = "TEXT")
    private String erro;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime iniciadoEm;

    private LocalDateTime finalizadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;
}
