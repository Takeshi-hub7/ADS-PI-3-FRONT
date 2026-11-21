package com.lojaroupas.util;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Máscaras de digitação para campos de texto: o usuário digita só os
 * números e o campo mostra a pontuação, como 000.000.000-00.
 */
public final class Mascaras {

    /** Quantidade máxima de dígitos de um telefone com DDD (celular). */
    private static final int MAX_DIGITOS_TELEFONE = 11;

    /** Impede a criação de instâncias da classe utilitária. */
    private Mascaras() {
    }

    /**
     * Aplica a máscara de CPF (000.000.000-00) ao campo.
     *
     * @param campo campo de texto
     */
    public static void aplicarCpf(TextField campo) {
        aplicar(campo, Formatador.TAMANHO_CPF, Mascaras::formatarCpfParcial);
    }

    /**
     * Aplica a máscara de telefone com DDD, aceitando fixo
     * ((00) 0000-0000) e celular ((00) 00000-0000).
     *
     * @param campo campo de texto
     */
    public static void aplicarTelefone(TextField campo) {
        aplicar(campo, MAX_DIGITOS_TELEFONE, Mascaras::formatarTelefoneParcial);
    }

    /**
     * Formata um CPF ainda incompleto, conforme os dígitos digitados.
     *
     * @param d dígitos (até 11)
     * @return texto com a pontuação correspondente
     */
    static String formatarCpfParcial(String d) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) {
                s.append('.');
            } else if (i == 9) {
                s.append('-');
            }
            s.append(d.charAt(i));
        }
        return s.toString();
    }

    /**
     * Formata um telefone ainda incompleto, conforme os dígitos digitados.
     *
     * @param d dígitos (até 11, com DDD)
     * @return texto com parênteses, espaço e hífen
     */
    static String formatarTelefoneParcial(String d) {
        if (d.isEmpty()) {
            return "";
        }
        if (d.length() <= 2) {
            return "(" + d;
        }
        String ddd = d.substring(0, 2);
        String numero = d.substring(2);
        int corte = d.length() == MAX_DIGITOS_TELEFONE ? 5 : 4;
        if (numero.length() > corte) {
            numero = numero.substring(0, corte) + "-" + numero.substring(corte);
        }
        return "(" + ddd + ") " + numero;
    }

    /**
     * Instala no campo um filtro que guarda só os dígitos e reescreve o
     * texto com a máscara a cada alteração.
     *
     * @param campo campo de texto
     * @param maxDigitos quantidade máxima de dígitos aceita
     * @param formatar função que aplica a máscara aos dígitos
     */
    private static void aplicar(TextField campo, int maxDigitos,
            UnaryOperator<String> formatar) {
        UnaryOperator<TextFormatter.Change> filtro = mudanca -> {
            if (!mudanca.isContentChange()) {
                return mudanca;
            }
            String digitos = Formatador.somenteDigitos(
                    mudanca.getControlNewText());
            if (digitos.length() > maxDigitos) {
                digitos = digitos.substring(0, maxDigitos);
            }
            String texto = formatar.apply(digitos);
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(texto);
            mudanca.setCaretPosition(texto.length());
            mudanca.setAnchor(texto.length());
            return mudanca;
        };
        String atual = campo.getText();
        campo.setTextFormatter(new TextFormatter<>(filtro));
        if (atual != null && !atual.isEmpty()) {
            campo.setText(atual);
        }
    }
}
