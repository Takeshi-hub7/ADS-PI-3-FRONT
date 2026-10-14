package com.lojaroupas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller da tela provisória exibida para módulos ainda não
 * implementados. O título e o subtítulo são definidos pelo menu.
 */
public class EmConstrucaoController {

    /** Rótulo do título da tela. */
    @FXML
    private Label rotuloTitulo;

    /** Rótulo do subtítulo da tela. */
    @FXML
    private Label rotuloSubtitulo;

    /**
     * Define os textos exibidos na tela provisória.
     *
     * @param titulo título da tela
     * @param subtitulo descrição curta do módulo
     */
    public void definirTextos(String titulo, String subtitulo) {
        rotuloTitulo.setText(titulo);
        rotuloSubtitulo.setText(subtitulo);
    }
}