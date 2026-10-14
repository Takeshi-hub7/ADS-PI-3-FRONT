package com.lojaroupas.controller;

import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Navegador;
import com.lojaroupas.util.Sessao;
import com.lojaroupas.util.Validador;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller da tela de login: servidor, e-mail e senha.
 */
public class LoginController {

    /** Armazena o último servidor usado (nunca a senha). */
    private static final Preferences PREFERENCIAS =
            Preferences.userNodeForPackage(LoginController.class);

    /** Chave sob a qual o endereço do servidor é guardado. */
    private static final String CHAVE_SERVIDOR = "urlServidor";

    /** Endereço sugerido quando não há servidor guardado. */
    private static final String SERVIDOR_PADRAO = "http://localhost:8080";

    /** Prefixo aceito para endereços sem criptografia. */
    private static final String PREFIXO_HTTP = "http://";

    /** Prefixo aceito para endereços com criptografia. */
    private static final String PREFIXO_HTTPS = "https://";

    /** Texto do botão em estado normal. */
    private static final String TEXTO_ENTRAR = "Entrar";

    /** Texto do botão enquanto a autenticação está em andamento. */
    private static final String TEXTO_ENTRANDO = "Entrando...";

    /** Campo com o endereço da API. */
    @FXML
    private TextField campoServidor;

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

    /** Preenche o campo de servidor com o último endereço usado. */
    @FXML
    private void initialize() {
        campoServidor.setText(
                PREFERENCIAS.get(CHAVE_SERVIDOR, SERVIDOR_PADRAO));
    }

    /**
     * Valida os campos e autentica o usuário.
     * Ligado ao botão "Entrar" e à tecla Enter no campo de senha.
     */
    @FXML
    private void entrar() {
        rotuloErro.setText("");

        String servidor = campoServidor.getText().trim();
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();

        String problema = validarCampos(servidor, email, senha);
        if (problema != null) {
            rotuloErro.setText(problema);
            return;
        }

        definirCarregando(true);
        ExecutorAssincrono.executar(
                () -> FabricaServicos.obterAutenticacaoService()
                        .autenticar(email, senha),
                usuario -> {
                    Sessao.iniciar(usuario, servidor);
                    PREFERENCIAS.put(CHAVE_SERVIDOR, servidor);
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
     * @param servidor endereço da API informado
     * @param email e-mail informado
     * @param senha senha informada
     * @return mensagem do primeiro problema, ou {@code null} se válido
     */
    private String validarCampos(String servidor, String email,
            String senha) {
        if (Validador.isVazio(servidor) || Validador.isVazio(email)
                || Validador.isVazio(senha)) {
            return "Preencha servidor, e-mail e senha.";
        }
        if (!servidor.startsWith(PREFIXO_HTTP)
                && !servidor.startsWith(PREFIXO_HTTPS)) {
            return "O servidor deve começar com http:// ou https://";
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