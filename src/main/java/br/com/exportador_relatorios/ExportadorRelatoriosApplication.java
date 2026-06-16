package br.com.exportador_relatorios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ExportadorRelatoriosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExportadorRelatoriosApplication.class, args);
	}

}
