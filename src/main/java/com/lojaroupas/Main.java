package com.lojaroupas;

import com.lojaroupas.util.Navegador;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação desktop.
 *
 * <p>Execute pelo Maven com {@code mvn javafx:run}. Executar esta classe
 * diretamente pelo botão "Run" do editor costuma falhar com a mensagem
 * "JavaFX runtime components are missing".</p>
 */
public class Main extends Application {

    /**
     * Inicia a interface gráfica exibindo a tela de login.
     *
     * @param palco janela principal fornecida pelo JavaFX
     */
    @Override
    public void start(Stage palco) {
        Navegador.iniciar(palco);
        Navegador.mostrarLogin();
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