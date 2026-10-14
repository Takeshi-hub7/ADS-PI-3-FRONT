package com.lojaroupas.controller;

import com.lojaroupas.util.Sessao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller da tela inicial. Futuramente exibirá o resumo do dia
 * (locações em andamento, devoluções e provas agendadas).
 */
public class InicioController {

    /** Rótulo de boas-vindas. */
    @FXML
    private Label rotuloBoasVindas;

    /** Preenche a mensagem de boas-vindas com o nome do usuário. */
    @FXML
    private void initialize() {
        String nome = Sessao.getUsuarioLogado().getNome();
        rotuloBoasVindas.setText("Bem-vindo(a), " + nome + ".");
    }
}