package com.lojaroupas.controller;

import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Navegador;
import com.lojaroupas.util.Sessao;
import com.lojaroupas.util.Validador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller da tela de login: e-mail e senha.
 *
 * <p>O endereço da API não é digitado aqui: vem da configuração da
 * aplicação (classe {@code Configuracao}).</p>
 */
public class LoginController {

    /** Texto do botão em estado normal. */
    private static final String TEXTO_ENTRAR = "Entrar";

    /** Texto do botão enquanto a autenticação está em andamento. */
    private static final String TEXTO_ENTRANDO = "Entrando...";

    /** Campo com o e-mail do usuário. */
    @FXML
    private TextField campoEmail;

    /** Campo com a senha do usuário. */
    @FXML
    private PasswordField campoSenha;

    /** Botão que dispara a autenticação. */
    @FXML
    private Button botaoEntrar;

    /** Rótulo onde as mensagens de erro são exibidas. */
    @FXML
    private Label rotuloErro;

    /**
     * Valida os campos e autentica o usuário.
     * Ligado ao botão "Entrar" e à tecla Enter no campo de senha.
     */
    @FXML
    private void entrar() {
        rotuloErro.setText("");

        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();

        String problema = validarCampos(email, senha);
        if (problema != null) {
            rotuloErro.setText(problema);
            return;
        }

        definirCarregando(true);
        ExecutorAssincrono.executar(
                () -> FabricaServicos.obterAutenticacaoService()
                        .autenticar(email, senha),
                usuario -> {
                    Sessao.iniciar(usuario);
                    Navegador.mostrarPrincipal();
                },
                erro -> {
                    rotuloErro.setText(
                            ExecutorAssincrono.obterMensagem(erro));
                    definirCarregando(false);
                });
    }

    /**
     * Confere o preenchimento dos campos do login.
     *
     * @param email e-mail informado
     * @param senha senha informada
     * @return mensagem do primeiro problema, ou {@code null} se válido
     */
    private String validarCampos(String email, String senha) {
        if (Validador.isVazio(email) || Validador.isVazio(senha)) {
            return "Preencha e-mail e senha.";
        }
        if (!Validador.isEmailValido(email)) {
            return "Informe um e-mail válido.";
        }
        return null;
    }

    /**
     * Alterna o botão entre os estados normal e carregando.
     *
     * @param carregando {@code true} enquanto a autenticação ocorre
     */
    private void definirCarregando(boolean carregando) {
        botaoEntrar.setDisable(carregando);
        botaoEntrar.setText(carregando ? TEXTO_ENTRANDO : TEXTO_ENTRAR);
    }
}