package com.lojaroupas.util;

import java.util.regex.Pattern;

/**
 * Validações dos dados digitados nos formulários.
 *
 * <p>Validar na tela melhora a experiência do usuário, mas a decisão
 * final é sempre da API.</p>
 */
public final class Validador {

    /** Quantidade de dígitos que antecedem os dígitos verificadores. */
    private static final int TAMANHO_BASE_CPF = 9;

    /** Módulo usado no cálculo dos dígitos verificadores do CPF. */
    private static final int MODULO_CPF = 11;

    /** Multiplicador aplicado à soma antes de calcular o resto. */
    private static final int MULTIPLICADOR_RESTO = 10;

    /** Resto que, por regra, resulta em dígito verificador zero. */
    private static final int RESTO_ZERADO = 10;

    /** Padrão simples de validação de e-mail. */
    private static final Pattern PADRAO_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    /** Impede a criação de instâncias da classe utilitária. */
    private Validador() {
    }

    /**
     * Verifica se um texto é nulo, vazio ou só tem espaços.
     *
     * @param texto texto a verificar
     * @return {@code true} se o texto não tem conteúdo útil
     */
    public static boolean isVazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /**
     * Verifica se um CPF é válido, conferindo os dígitos verificadores.
     *
     * @param cpf CPF com ou sem formatação
     * @return {@code true} se o CPF é válido
     */
    public static boolean isCpfValido(String cpf) {
        String digitos = Formatador.somenteDigitos(cpf);
        if (digitos.length() != Formatador.TAMANHO_CPF
                || digitos.chars().distinct().count() == 1) {
            return false;
        }
        int primeiro = calcularDigitoVerificador(digitos, TAMANHO_BASE_CPF);
        int segundo = calcularDigitoVerificador(digitos,
                TAMANHO_BASE_CPF + 1);
        int informadoPrimeiro =
                Character.getNumericValue(digitos.charAt(TAMANHO_BASE_CPF));
        int informadoSegundo = Character.getNumericValue(
                digitos.charAt(TAMANHO_BASE_CPF + 1));
        return primeiro == informadoPrimeiro && segundo == informadoSegundo;
    }

    /**
     * Verifica se um e-mail tem formato válido.
     *
     * @param email e-mail a verificar
     * @return {@code true} se o formato é válido
     */
    public static boolean isEmailValido(String email) {
        return email != null && PADRAO_EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Calcula um dígito verificador do CPF.
     *
     * @param digitos CPF contendo somente dígitos
     * @param quantidade quantidade de dígitos usados no cálculo
     * @return dígito verificador calculado
     */
    private static int calcularDigitoVerificador(String digitos,
            int quantidade) {
        int soma = 0;
        for (int posicao = 0; posicao < quantidade; posicao++) {
            int valor = Character.getNumericValue(digitos.charAt(posicao));
            soma += valor * (quantidade + 1 - posicao);
        }
        int resto = (soma * MULTIPLICADOR_RESTO) % MODULO_CPF;
        return resto == RESTO_ZERADO ? 0 : resto;
    }
}