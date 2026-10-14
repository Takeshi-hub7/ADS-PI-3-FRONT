package com.lojaroupas.controller;

import com.lojaroupas.model.PerfilAcesso;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.Configuracao;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Navegador;
import com.lojaroupas.util.Sessao;
import com.lojaroupas.util.Tela;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Controller da moldura do sistema: menu lateral e área de conteúdo.
 *
 * <p>Exibe somente os itens permitidos ao perfil logado. Esconder botões
 * é uma conveniência: a proteção real é feita pela API.</p>
 */
public class PrincipalController {

    /** Arquivo FXML da tela inicial. */
    private static final String TELA_INICIO = "InicioView.fxml";

    /** Arquivo FXML da tela de clientes. */
    private static final String TELA_CLIENTES = "ClientesView.fxml";

    /** Arquivo FXML da tela provisória. */
    private static final String TELA_EM_CONSTRUCAO = "EmConstrucaoView.fxml";

    /** Classe de estilo aplicada ao item de menu ativo. */
    private static final String CLASSE_ATIVO = "ativo";

    /** Título dos diálogos de erro ao abrir telas. */
    private static final String TITULO_ERRO = "Erro ao abrir a tela";

    /** Rótulo com o nome do usuário logado. */
    @FXML
    private Label rotuloUsuario;

    /** Rótulo com o perfil do usuário logado. */
    @FXML
    private Label rotuloPerfil;

    /** Rótulo com o endereço do servidor. */
    @FXML
    private Label rotuloServidor;

    /** Área onde as telas são exibidas. */
    @FXML
    private StackPane areaConteudo;

    /** Item de menu: início. */
    @FXML
    private Button botaoInicio;

    /** Item de menu: trajes. */
    @FXML
    private Button botaoTrajes;

    /** Item de menu: peças e estoque. */
    @FXML
    private Button botaoPecas;

    /** Item de menu: locações. */
    @FXML
    private Button botaoLocacoes;

    /** Item de menu: agenda de provas. */
    @FXML
    private Button botaoAgenda;

    /** Item de menu: clientes. */
    @FXML
    private Button botaoClientes;

    /** Item de menu: funcionários (somente administrador). */
    @FXML
    private Button botaoFuncionarios;

    /** Item de menu: financeiro (somente administrador). */
    @FXML
    private Button botaoFinanceiro;

    /** Todos os itens de menu, usados para marcar o item ativo. */
    private List<Button> botoesMenu;

    /** Preenche o cabeçalho, aplica as permissões e abre o início. */
    @FXML
    private void initialize() {
        PerfilAcesso perfil = Sessao.getUsuarioLogado().getPerfilAcesso();
        rotuloUsuario.setText(Sessao.getUsuarioLogado().getNome());
        rotuloPerfil.setText(
                perfil == PerfilAcesso.ADMIN ? "Proprietário" : "Funcionário");
            rotuloServidor.setText(Configuracao.descreverServidor());

        botoesMenu = List.of(botaoInicio, botaoTrajes, botaoPecas,
                botaoLocacoes, botaoAgenda, botaoClientes,
                botaoFuncionarios, botaoFinanceiro);

        if (!Sessao.isAdministrador()) {
            esconder(botaoFuncionarios);
            esconder(botaoFinanceiro);
        }

        abrirInicio();
    }

    /** Abre a tela inicial. */
    @FXML
    private void abrirInicio() {
        abrirTela(TELA_INICIO, botaoInicio);
    }

    /** Abre a tela de trajes (provisória). */
    @FXML
    private void abrirTrajes() {
        abrirEmConstrucao("Trajes",
                "Cadastro de trajes e controle de status", botaoTrajes);
    }

    /** Abre a tela de peças e estoque (provisória). */
    @FXML
    private void abrirPecas() {
        abrirEmConstrucao("Peças e Estoque",
                "Peças de cada traje e quantidade em estoque", botaoPecas);
    }

    /** Abre a tela de locações (provisória). */
    @FXML
    private void abrirLocacoes() {
        abrirEmConstrucao("Locações",
                "Trajes alugados, retiradas e devoluções", botaoLocacoes);
    }

    /** Abre a tela da agenda de provas (provisória). */
    @FXML
    private void abrirAgenda() {
        abrirEmConstrucao("Agenda de Provas",
                "Horários marcados para prova e ajuste", botaoAgenda);
    }

    /** Abre a tela de clientes. */
    @FXML
    private void abrirClientes() {
        abrirTela(TELA_CLIENTES, botaoClientes);
    }

    /** Abre a tela de funcionários (provisória; só administrador). */
    @FXML
    private void abrirFuncionarios() {
        if (!exigirAdministrador()) {
            return;
        }
        abrirEmConstrucao("Funcionários",
                "Cadastro de funcionários (exclusivo do proprietário)",
                botaoFuncionarios);
    }

    /** Abre a tela financeira (provisória; só administrador). */
    @FXML
    private void abrirFinanceiro() {
        if (!exigirAdministrador()) {
            return;
        }
        abrirEmConstrucao("Financeiro",
                "Receitas e resumo financeiro (exclusivo do proprietário)",
                botaoFinanceiro);
    }

    /** Encerra a sessão, após confirmação, e volta ao login. */
    @FXML
    private void sair() {
        if (Alertas.confirmar("Sair", "Deseja encerrar a sessão?")) {
            Sessao.encerrar();
            Navegador.mostrarLogin();
        }
    }

    /**
     * Carrega uma tela FXML e a exibe na área de conteúdo.
     *
     * @param arquivo nome do arquivo FXML
     * @param botao item de menu a destacar como ativo
     */
    private void abrirTela(String arquivo, Button botao) {
        try {
            exibir(Navegador.carregar(arquivo), botao);
        } catch (IllegalStateException e) {
            Alertas.exibirErro(TITULO_ERRO,
                    ExecutorAssincrono.obterMensagem(e));
        }
    }

    /**
     * Exibe a tela provisória com título e subtítulo próprios.
     *
     * @param titulo título da tela
     * @param subtitulo descrição curta do módulo
     * @param botao item de menu a destacar como ativo
     */
    private void abrirEmConstrucao(String titulo, String subtitulo,
            Button botao) {
        try {
            Tela<EmConstrucaoController> tela =
                    Navegador.carregarTela(TELA_EM_CONSTRUCAO);
            tela.controlador().definirTextos(titulo, subtitulo);
            exibir(tela.raiz(), botao);
        } catch (IllegalStateException e) {
            Alertas.exibirErro(TITULO_ERRO,
                    ExecutorAssincrono.obterMensagem(e));
        }
    }

    /**
     * Coloca uma tela na área de conteúdo e marca o menu ativo.
     *
     * @param tela componente raiz da tela
     * @param botao item de menu a destacar como ativo
     */
    private void exibir(Parent tela, Button botao) {
        areaConteudo.getChildren().setAll(tela);
        for (Button item : botoesMenu) {
            item.getStyleClass().remove(CLASSE_ATIVO);
        }
        botao.getStyleClass().add(CLASSE_ATIVO);
    }

    /**
     * Verifica se o usuário é administrador, avisando se não for.
     *
     * @return {@code true} se o acesso é permitido
     */
    private boolean exigirAdministrador() {
        if (Sessao.isAdministrador()) {
            return true;
        }
        Alertas.exibirErro("Acesso negado",
                "Esta tela é exclusiva do proprietário.");
        return false;
    }

    /**
     * Esconde um botão sem deixar espaço vazio no menu.
     *
     * @param botao botão a esconder
     */
    private void esconder(Button botao) {
        botao.setVisible(false);
        botao.setManaged(false);
    }
}