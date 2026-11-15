package com.lojaroupas;

import com.lojaroupas.util.Navegador;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação desktop.
 *
 * <p>Execute pelo Maven com {@code mvn javafx:run}. Executar esta classe
 * diretamente pelo botão "Run" do editor costuma falhar com a mensagem
 * "JavaFX runtime components are missing".</p>
 */
public class Main extends Application {

    /** Registro de avisos da inicialização. */
    private static final Logger REGISTRO =
            Logger.getLogger(Main.class.getName());

    /**
     * Arquivos da fonte Inter usados pela folha de estilo (licença SIL
     * Open Font License, em {@code fontes/Inter-LICENSE.txt}).
     */
    private static final List<String> FONTES = List.of(
            "/fontes/Inter-Regular.ttf",
            "/fontes/Inter-SemiBold.ttf",
            "/fontes/Inter-Bold.ttf");

    /**
     * Inicia a interface gráfica exibindo a tela de login.
     *
     * @param palco janela principal fornecida pelo JavaFX
     */
    @Override
    public void start(Stage palco) {
        carregarFontes();
        Navegador.iniciar(palco);
        Navegador.mostrarLogin();
    }

    /**
     * Registra as fontes do projeto no JavaFX. Se alguma falhar, a
     * aplicação segue com a fonte padrão do sistema.
     */
    private static void carregarFontes() {
        for (String caminho : FONTES) {
            try (InputStream entrada = Main.class.getResourceAsStream(caminho)) {
                if (entrada == null || Font.loadFont(entrada, 14) == null) {
                    REGISTRO.warning("Fonte não carregada: " + caminho);
                }
            } catch (IOException e) {
                REGISTRO.log(Level.WARNING,
                        "Falha ao ler a fonte " + caminho, e);
            }
        }
    }

    /**
     * Método de entrada da JVM.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}