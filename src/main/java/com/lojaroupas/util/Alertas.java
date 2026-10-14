package com.lojaroupas.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

/**
 * Diálogos padronizados de informação, erro e confirmação.
 */
public final class Alertas {

    /** Texto do botão de confirmação positiva. */
    private static final String TEXTO_SIM = "Sim";

    /** Texto do botão de confirmação negativa. */
    private static final String TEXTO_NAO = "Não";

    /** Impede a criação de instâncias da classe utilitária. */
    private Alertas() {
    }

    /**
     * Exibe um diálogo de informação.
     *
     * @param titulo título da janela
     * @param mensagem texto exibido
     */
    public static void exibirInformacao(String titulo, String mensagem) {
        exibir(Alert.AlertType.INFORMATION, titulo, mensagem);
    }

    /**
     * Exibe um diálogo de erro.
     *
     * @param titulo título da janela
     * @param mensagem texto exibido
     */
    public static void exibirErro(String titulo, String mensagem) {
        exibir(Alert.AlertType.ERROR, titulo, mensagem);
    }

    /**
     * Pergunta Sim ou Não ao usuário.
     *
     * @param titulo título da janela
     * @param mensagem pergunta exibida
     * @return {@code true} se o usuário escolheu "Sim"
     */
    public static boolean confirmar(String titulo, String mensagem) {
        ButtonType sim = new ButtonType(TEXTO_SIM, ButtonData.YES);
        ButtonType nao = new ButtonType(TEXTO_NAO, ButtonData.NO);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem,
                sim, nao);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(nao) == sim;
    }

    /**
     * Exibe um diálogo simples e aguarda o usuário fechá-lo.
     *
     * @param tipo tipo do diálogo
     * @param titulo título da janela
     * @param mensagem texto exibido
     */
    private static void exibir(Alert.AlertType tipo, String titulo,
            String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}