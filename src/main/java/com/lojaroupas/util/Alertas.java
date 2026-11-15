package com.lojaroupas.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Diálogos padronizados de informação, erro e confirmação, e o aviso
 * rápido que some sozinho.
 */
public final class Alertas {

    /** Texto do botão de confirmação positiva. */
    private static final String TEXTO_SIM = "Sim";

    /** Texto do botão de confirmação negativa. */
    private static final String TEXTO_NAO = "Não";

    /** Tempo que o aviso rápido fica visível. */
    private static final Duration TEMPO_AVISO = Duration.seconds(2.5);

    /** Duração do esmaecimento do aviso rápido. */
    private static final Duration TEMPO_SUMIR = Duration.millis(400);

    /** Distância do aviso até a borda inferior da janela. */
    private static final double MARGEM_AVISO = 40.0;

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
     * Mostra um aviso curto na parte de baixo da janela, que some
     * sozinho. Serve para confirmações que não exigem resposta, como
     * "Cliente salvo".
     *
     * @param mensagem texto exibido
     */
    public static void mostrarAviso(String mensagem) {
        Stage janela = Navegador.getPalco();
        Label texto = new Label(mensagem);
        texto.getStyleClass().add("aviso");
        String css = Navegador.getEnderecoCss();
        if (css != null) {
            texto.getStylesheets().add(css);
        }
        Popup aviso = new Popup();
        aviso.getContent().add(texto);
        aviso.show(janela);
        aviso.setX(janela.getX()
                + (janela.getWidth() - texto.getWidth()) / 2);
        aviso.setY(janela.getY() + janela.getHeight()
                - texto.getHeight() - MARGEM_AVISO);

        FadeTransition sumir = new FadeTransition(TEMPO_SUMIR, texto);
        sumir.setToValue(0);
        SequentialTransition animacao = new SequentialTransition(
                new PauseTransition(TEMPO_AVISO), sumir);
        animacao.setOnFinished(evento -> aviso.hide());
        animacao.play();
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
        aplicarEstilo(alerta);
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
        aplicarEstilo(alerta);
        alerta.showAndWait();
    }

    /**
     * Aplica a folha de estilo da aplicação ao diálogo, que abre numa
     * janela própria e não herda o estilo da janela principal.
     *
     * @param alerta diálogo a estilizar
     */
    private static void aplicarEstilo(Alert alerta) {
        String css = Navegador.getEnderecoCss();
        if (css != null) {
            alerta.getDialogPane().getStylesheets().add(css);
        }
        if (Navegador.getPalco() != null) {
            alerta.initOwner(Navegador.getPalco());
        }
    }
}