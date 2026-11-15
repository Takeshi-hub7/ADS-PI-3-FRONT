package com.lojaroupas.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Funções de formatação de textos exibidos na interface.
 */
public final class Formatador {

    /** Quantidade de dígitos de um CPF. */
    public static final int TAMANHO_CPF = 11;

    /** Expressão que identifica qualquer caractere que não é dígito. */
    private static final String REGEX_NAO_DIGITO = "\\D";

    /** Expressão que divide os 11 dígitos do CPF em quatro grupos. */
    private static final String REGEX_CPF =
            "(\\d{3})(\\d{3})(\\d{3})(\\d{2})";

    /** Máscara aplicada aos grupos do CPF. */
    private static final String MASCARA_CPF = "$1.$2.$3-$4";

    /** Formato de exibição das datas. */
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Impede a criação de instâncias da classe utilitária. */
    private Formatador() {
    }

    /**
     * Remove todos os caracteres que não são dígitos.
     *
     * @param texto texto de entrada, possivelmente {@code null}
     * @return somente os dígitos; texto vazio se a entrada for nula
     */
    public static String somenteDigitos(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll(REGEX_NAO_DIGITO, "");
    }

    /**
     * Formata um CPF como 000.000.000-00.
     *
     * @param cpf CPF com ou sem formatação
     * @return CPF formatado; o texto original se não tiver 11 dígitos
     */
    public static String formatarCpf(String cpf) {
        String digitos = somenteDigitos(cpf);
        if (digitos.length() != TAMANHO_CPF) {
            return cpf;
        }
        return digitos.replaceAll(REGEX_CPF, MASCARA_CPF);
    }

    /**
     * Formata uma data como dd/MM/aaaa.
     *
     * @param data data a formatar, possivelmente {@code null}
     * @return data formatada; texto vazio se a data for nula
     */
    public static String formatarData(LocalDate data) {
        if (data == null) {
            return "";
        }
        return FORMATO_DATA.format(data);
    }

    /**
     * Formata um valor em reais, como R$ 1.250,00.
     *
     * @param valor valor a formatar, possivelmente {@code null}
     * @return valor formatado; R$ 0,00 se o valor for nulo
     */
    public static String formatarMoeda(BigDecimal valor) {
        NumberFormat formato =
                NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }
}
