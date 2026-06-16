package br.com.exportador_relatorios.util;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CsvUtils {

    private CsvUtils() {
    }

    public static String linha(String... campos) {
        return String.join(";", campos);
    }

    public static String texto(Object valor) {
        if (valor == null) {
            return "";
        }

        String texto = valor.toString();

        texto = texto.replace("\"", "\"\"");

        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto + "\"";
        }

        return texto;
    }

    public static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return "";
        }

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        DecimalFormat formatador = new DecimalFormat("#,##0.00", simbolos);

        return formatador.format(valor);
    }
}
