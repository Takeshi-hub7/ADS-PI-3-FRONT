package com.lojaroupas.util;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Carrega telas FXML e controla a troca de cenas da janela.
 *
 * <p>O CSS é aplicado aqui, na cena, uma única vez. Por isso os arquivos
 * FXML não precisam declarar folhas de estilo.</p>
 */
public final class Navegador {

    /** Pasta (no classpath) onde ficam os arquivos FXML. */
    private static final String PASTA_VIEWS = "/com/lojaroupas/view/";

    /** Caminho (no classpath) da folha de estilo da aplicação. */
    private static final String ARQUIVO_CSS = "/css/estilo.css";

    /** Título exibido na barra da janela. */
    private static final String TITULO_JANELA = "TrajeLoc";

    /** Arquivo FXML da tela de login. */
    private static final String TELA_LOGIN = "LoginView.fxml";

    /** Arquivo FXML da moldura principal do sistema. */
    private static final String TELA_PRINCIPAL = "PrincipalView.fxml";

    /** Largura inicial da janela de login. */
    private static final double LARGURA_LOGIN = 520.0;

    /** Altura inicial da janela de login. */
    private static final double ALTURA_LOGIN = 620.0;

    /** Largura mínima da janela de login. */
    private static final double LARGURA_MINIMA_LOGIN = 420.0;

    /** Altura mínima da janela de login. */
    private static final double ALTURA_MINIMA_LOGIN = 560.0;

    /** Largura inicial da janela principal. */
    private static final double LARGURA_PRINCIPAL = 1200.0;

    /** Altura inicial da janela principal. */
    private static final double ALTURA_PRINCIPAL = 760.0;

    /** Largura mínima da janela principal. */
    private static final double LARGURA_MINIMA_PRINCIPAL = 1000.0;

    /** Altura mínima da janela principal. */
    private static final double ALTURA_MINIMA_PRINCIPAL = 650.0;

    /** Janela da aplicação, definida uma vez por {@link #iniciar}. */
    private static Stage palco;

    /** Impede a criação de instâncias da classe utilitária. */
    private Navegador() {
    }

    /**
     * Registra a janela da aplicação. Deve ser chamado uma única vez,
     * pela classe {@code Main}.
     *
     * @param janela janela fornecida pelo JavaFX
     */
    public static void iniciar(Stage janela) {
        palco = janela;
        palco.setTitle(TITULO_JANELA);
    }

    /**
     * Carrega um arquivo FXML e devolve a tela junto com seu controller.
     *
     * @param <T> tipo do controller esperado
     * @param nomeArquivo nome do arquivo na pasta de views
     * @return tela carregada
     * @throws IllegalStateException se o arquivo não existir ou for
     *         inválido
     */
    public static <T> Tela<T> carregarTela(String nomeArquivo) {
        URL url = Navegador.class.getResource(PASTA_VIEWS + nomeArquivo);
        if (url == null) {
            throw new IllegalStateException(
                    "FXML não encontrado: " + PASTA_VIEWS + nomeArquivo);
        }
        FXMLLoader carregador = new FXMLLoader(url);
        try {
            Parent raiz = carregador.load();
            T controlador = carregador.getController();
            return new Tela<>(raiz, controlador);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar "
                    + nomeArquivo + ": " + e.getMessage(), e);
        }
    }

    /**
     * Carrega um arquivo FXML quando o controller não é necessário.
     *
     * @param nomeArquivo nome do arquivo na pasta de views
     * @return componente raiz da tela
     * @throws IllegalStateException se o arquivo não existir ou for
     *         inválido
     */
    public static Parent carregar(String nomeArquivo) {
        Tela<Object> tela = carregarTela(nomeArquivo);
        return tela.raiz();
    }

    /** Exibe a tela de login na janela. */
    public static void mostrarLogin() {
        trocarCena(carregar(TELA_LOGIN), LARGURA_LOGIN, ALTURA_LOGIN,
                LARGURA_MINIMA_LOGIN, ALTURA_MINIMA_LOGIN);
    }

    /** Exibe a moldura principal (menu lateral e conteúdo). */
    public static void mostrarPrincipal() {
        trocarCena(carregar(TELA_PRINCIPAL), LARGURA_PRINCIPAL,
                ALTURA_PRINCIPAL, LARGURA_MINIMA_PRINCIPAL,
                ALTURA_MINIMA_PRINCIPAL);
    }

    /**
     * Substitui a cena da janela, aplicando a folha de estilo.
     *
     * @param raiz componente raiz da nova cena
     * @param largura largura inicial da janela
     * @param altura altura inicial da janela
     * @param larguraMinima largura mínima da janela
     * @param alturaMinima altura mínima da janela
     */
    private static void trocarCena(Parent raiz, double largura,
            double altura, double larguraMinima, double alturaMinima) {
        URL css = Navegador.class.getResource(ARQUIVO_CSS);
        if (css == null) {
            throw new IllegalStateException(
                    "CSS não encontrado: " + ARQUIVO_CSS);
        }
        Scene cena = new Scene(raiz, largura, altura);
        cena.getStylesheets().add(css.toExternalForm());
        palco.setMinWidth(larguraMinima);
        palco.setMinHeight(alturaMinima);
        palco.setScene(cena);
        palco.sizeToScene();
        palco.centerOnScreen();
        palco.show();
    }
}