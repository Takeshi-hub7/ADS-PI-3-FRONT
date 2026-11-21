package com.lojaroupas.controller;

import com.lojaroupas.model.Funcionario;
import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.service.FuncionarioService;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Formatador;
import com.lojaroupas.util.Mascaras;
import com.lojaroupas.util.Validador;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller da tela de funcionários (RF-35, exclusiva do administrador):
 * lista com busca e filtro de situação, cadastro com login, edição e
 * ativação ou desativação sem apagar o cadastro.
 */
public class FuncionariosController {

    /** Limites iguais aos de {@code FuncionarioRequest} e do usuário na API. */
    private static final int MAX_NOME_LOGIN = 60;
    private static final int MAX_CURTO = 20;
    private static final int MAX_ESTADO_CIVIL = 30;
    private static final int MAX_EMAIL = 40;
    private static final int MIN_SENHA = 6;

    /** Opções do filtro de situação. */
    private static final String SITUACAO_TODOS = "Todos";
    private static final String SITUACAO_ATIVOS = "Ativos";
    private static final String SITUACAO_INATIVOS = "Inativos";

    /** Campo de busca. */
    @FXML
    private TextField campoBusca;

    /** Filtro por situação. */
    @FXML
    private ComboBox<String> campoFiltroSituacao;

    /** Tabela de funcionários. */
    @FXML
    private TableView<Funcionario> tabelaFuncionarios;

    /** Coluna do nome. */
    @FXML
    private TableColumn<Funcionario, String> colunaNome;

    /** Coluna do CPF. */
    @FXML
    private TableColumn<Funcionario, String> colunaCpf;

    /** Coluna do telefone. */
    @FXML
    private TableColumn<Funcionario, String> colunaTelefone;

    /** Coluna da situação. */
    @FXML
    private TableColumn<Funcionario, String> colunaSituacao;

    /** Contagem de funcionários exibidos. */
    @FXML
    private Label rotuloTotal;

    /** Título do formulário. */
    @FXML
    private Label rotuloTituloFormulario;

    /** Campos do formulário. */
    @FXML
    private TextField campoNome;
    @FXML
    private TextField campoCpf;
    @FXML
    private TextField campoTelefone;
    @FXML
    private TextField campoRg;
    @FXML
    private TextField campoCtps;
    @FXML
    private ComboBox<String> campoSexo;
    @FXML
    private ComboBox<String> campoEstadoCivil;
    @FXML
    private DatePicker campoNascimento;
    @FXML
    private TextField campoDependentes;
    @FXML
    private TextField campoSalario;

    /** Bloco de login, exibido só no cadastro novo. */
    @FXML
    private VBox blocoLogin;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenha;

    /** Situação do funcionário em edição. */
    @FXML
    private Label rotuloSituacao;

    /** Mensagens de erro do formulário. */
    @FXML
    private Label rotuloErro;

    /** Botão de salvar. */
    @FXML
    private Button botaoSalvar;

    /** Botão que ativa ou desativa o funcionário em edição. */
    @FXML
    private Button botaoAtivacao;

    /** Serviço de funcionários. */
    private final FuncionarioService servico =
            FabricaServicos.obterFuncionarioService();

    /** Funcionários carregados. */
    private final ObservableList<Funcionario> funcionarios =
            FXCollections.observableArrayList();

    /** Visão filtrada. */
    private FilteredList<Funcionario> filtrados;

    /** Funcionário em edição; {@code null} no cadastro novo. */
    private Funcionario emEdicao;

    /** Configura a tela e carrega a lista. */
    @FXML
    private void initialize() {
        Mascaras.aplicarCpf(campoCpf);
        Mascaras.aplicarTelefone(campoTelefone);
        campoSexo.getItems().setAll("Feminino", "Masculino", "Outro");
        campoEstadoCivil.getItems().setAll("Solteiro(a)", "Casado(a)",
                "Divorciado(a)", "Viúvo(a)", "União estável");
        campoEstadoCivil.setEditable(true);
        campoFiltroSituacao.getItems().setAll(SITUACAO_TODOS,
                SITUACAO_ATIVOS, SITUACAO_INATIVOS);
        campoFiltroSituacao.setValue(SITUACAO_TODOS);
        campoBusca.textProperty().addListener((o, a, n) -> aplicarFiltro());
        campoFiltroSituacao.valueProperty()
                .addListener((o, a, n) -> aplicarFiltro());
        configurarTabela();
        novo();
        carregar();
    }

    /** Recarrega a lista. */
    @FXML
    private void carregar() {
        ExecutorAssincrono.executar(
                servico::listarTodos,
                lista -> {
                    funcionarios.setAll(lista);
                    aplicarFiltro();
                },
                erro -> Alertas.exibirErro(
                        "Não foi possível carregar os funcionários",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Limpa o formulário para um cadastro novo. */
    @FXML
    private void novo() {
        emEdicao = null;
        tabelaFuncionarios.getSelectionModel().clearSelection();
        for (TextField campo : List.of(campoNome, campoCpf, campoTelefone,
                campoRg, campoCtps, campoDependentes, campoSalario,
                campoEmail, campoSenha)) {
            campo.clear();
        }
        campoSexo.setValue(null);
        campoEstadoCivil.setValue(null);
        campoEstadoCivil.getEditor().clear();
        campoNascimento.setValue(null);
        mostrarLogin(true);
        rotuloSituacao.setText("");
        rotuloErro.setText("");
        rotuloTituloFormulario.setText("Novo funcionário");
        botaoAtivacao.setVisible(false);
        botaoAtivacao.setManaged(false);
    }

    /** Valida e salva (cadastro novo ou edição). */
    @FXML
    private void salvar() {
        String problema = validar();
        if (problema != null) {
            rotuloErro.setText(problema);
            return;
        }
        rotuloErro.setText("");
        Funcionario f = new Funcionario(
                emEdicao == null ? null : emEdicao.idFuncionario(),
                campoNome.getText().trim(),
                emEdicao == null ? null : emEdicao.idUsuario(),
                campoCpf.getText(),
                campoRg.getText().trim(),
                campoTelefone.getText(),
                campoCtps.getText().trim(),
                lerInteiro(campoDependentes.getText()),
                campoSexo.getValue(),
                lerValor(campoSalario.getText()),
                campoNascimento.getValue(),
                estadoCivilDigitado(),
                emEdicao == null || emEdicao.ativo());
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();

        botaoSalvar.setDisable(true);
        ExecutorAssincrono.executar(
                () -> emEdicao == null
                        ? servico.cadastrar(f, email, senha)
                        : servico.atualizar(f),
                salvo -> {
                    botaoSalvar.setDisable(false);
                    Alertas.mostrarAviso(salvo.nome() + " salvo(a) com sucesso.");
                    novo();
                    carregar();
                },
                erro -> {
                    botaoSalvar.setDisable(false);
                    rotuloErro.setText(ExecutorAssincrono.obterMensagem(erro));
                });
    }

    /** Ativa ou desativa o funcionário em edição, após confirmação. */
    @FXML
    private void alternarAtivacao() {
        if (emEdicao == null) {
            return;
        }
        boolean ativar = !emEdicao.ativo();
        String acao = ativar ? "reativar" : "desativar";
        String pergunta = "Deseja " + acao + " " + emEdicao.nome() + "?"
                + (ativar ? "" : "\nO cadastro continua guardado e ele(a) "
                        + "deixa de aparecer como atendente nas locações.");
        if (!Alertas.confirmar(ativar ? "Reativar funcionário"
                : "Desativar funcionário", pergunta)) {
            return;
        }
        Integer id = emEdicao.idFuncionario();
        ExecutorAssincrono.executar(
                () -> servico.definirAtivo(id, ativar),
                f -> {
                    Alertas.mostrarAviso(f.nome() + (f.ativo()
                            ? " reativado(a)." : " desativado(a)."));
                    novo();
                    carregar();
                },
                erro -> Alertas.exibirErro("Não foi possível " + acao,
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Liga as colunas aos dados e cria o filtro e a ordenação. */
    private void configurarTabela() {
        colunaNome.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().nome()));
        colunaCpf.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().cpf()));
        colunaTelefone.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().telefone()));
        colunaSituacao.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().ativo() ? "Ativo" : "Inativo"));
        colunaSituacao.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vazio) {
                super.updateItem(texto, vazio);
                setText(vazio ? null : texto);
                getStyleClass().remove("situacao-inativo");
                if (!vazio && "Inativo".equals(texto)) {
                    getStyleClass().add("situacao-inativo");
                }
            }
        });
        filtrados = new FilteredList<>(funcionarios, f -> true);
        SortedList<Funcionario> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(
                tabelaFuncionarios.comparatorProperty());
        tabelaFuncionarios.setItems(ordenados);
        tabelaFuncionarios.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaFuncionarios.setPlaceholder(
                new Label("Nenhum funcionário encontrado."));
        tabelaFuncionarios.getSortOrder().add(colunaNome);
        tabelaFuncionarios.getSelectionModel().selectedItemProperty()
                .addListener((o, a, f) -> {
                    if (f != null) {
                        editar(f);
                    }
                });
    }

    /** Aplica busca e filtro de situação. */
    private void aplicarFiltro() {
        String termo = campoBusca.getText() == null ? ""
                : campoBusca.getText().trim().toLowerCase(Locale.ROOT);
        String digitos = Formatador.somenteDigitos(termo);
        String situacao = campoFiltroSituacao.getValue();
        filtrados.setPredicate(f ->
                (SITUACAO_TODOS.equals(situacao)
                        || (SITUACAO_ATIVOS.equals(situacao) && f.ativo())
                        || (SITUACAO_INATIVOS.equals(situacao) && !f.ativo()))
                && (termo.isEmpty()
                        || f.nome().toLowerCase(Locale.ROOT).contains(termo)
                        || (!digitos.isEmpty() && Formatador
                                .somenteDigitos(f.cpf()).contains(digitos))));
        long ativos = funcionarios.stream().filter(Funcionario::ativo).count();
        rotuloTotal.setText(filtrados.size() + " exibidos · " + ativos
                + " ativos de " + funcionarios.size());
    }

    /**
     * Carrega um funcionário no formulário.
     *
     * @param f funcionário selecionado
     */
    private void editar(Funcionario f) {
        emEdicao = f;
        campoNome.setText(f.nome());
        campoCpf.setText(f.cpf());
        campoTelefone.setText(f.telefone());
        campoRg.setText(f.rg());
        campoCtps.setText(f.ctps());
        campoSexo.setValue(f.sexo() == null || f.sexo().isBlank()
                ? null : f.sexo());
        campoEstadoCivil.setValue(f.estadoCivil());
        campoEstadoCivil.getEditor().setText(f.estadoCivil());
        campoNascimento.setValue(f.dataNascimento());
        campoDependentes.setText(String.valueOf(f.dependente()));
        campoSalario.setText(f.salario() == null ? ""
                : f.salario().toPlainString().replace('.', ','));
        mostrarLogin(false);
        rotuloTituloFormulario.setText("Editando funcionário");
        rotuloSituacao.setText("Situação: " + (f.ativo() ? "ativo" : "inativo"));
        rotuloErro.setText("");
        botaoAtivacao.setText(f.ativo() ? "Desativar" : "Reativar");
        botaoAtivacao.getStyleClass().removeAll("botao-perigo",
                "botao-secundario");
        botaoAtivacao.getStyleClass().add(f.ativo()
                ? "botao-perigo" : "botao-secundario");
        botaoAtivacao.setVisible(true);
        botaoAtivacao.setManaged(true);
    }

    /**
     * Mostra ou esconde o bloco de login.
     *
     * @param mostrar {@code true} no cadastro novo
     */
    private void mostrarLogin(boolean mostrar) {
        blocoLogin.setVisible(mostrar);
        blocoLogin.setManaged(mostrar);
    }

    /**
     * Estado civil digitado ou escolhido.
     *
     * @return texto ou vazio
     */
    private String estadoCivilDigitado() {
        String texto = campoEstadoCivil.getEditor().getText();
        if (Validador.isVazio(texto) && campoEstadoCivil.getValue() != null) {
            texto = campoEstadoCivil.getValue();
        }
        return texto == null ? "" : texto.trim();
    }

    /**
     * Lê um inteiro; vazio vale zero.
     *
     * @param texto texto digitado
     * @return número, ou {@code -1} se inválido
     */
    private int lerInteiro(String texto) {
        if (Validador.isVazio(texto)) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Lê um valor em reais (vírgula ou ponto); vazio vale {@code null}.
     *
     * @param texto texto digitado
     * @return valor ou {@code null}
     */
    private BigDecimal lerValor(String texto) {
        if (Validador.isVazio(texto)) {
            return null;
        }
        String limpo = texto.trim().replace("R$", "").replace(" ", "");
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(',', '.');
        }
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return BigDecimal.valueOf(-1);
        }
    }

    /**
     * Confere o formulário com as regras da API.
     *
     * @return primeira mensagem de problema, ou {@code null}
     */
    private String validar() {
        String nome = campoNome.getText();
        if (Validador.isVazio(nome)) {
            return "Informe o nome do funcionário.";
        }
        if (emEdicao == null && nome.trim().length() > MAX_NOME_LOGIN) {
            return "O nome deve ter no máximo " + MAX_NOME_LOGIN + " caracteres.";
        }
        if (!Validador.isCpfValido(campoCpf.getText())) {
            return "CPF inválido.";
        }
        for (TextField campo : List.of(campoRg, campoCtps)) {
            if (campo.getText() != null
                    && campo.getText().trim().length() > MAX_CURTO) {
                return "RG e CTPS podem ter no máximo " + MAX_CURTO
                        + " caracteres.";
            }
        }
        if (estadoCivilDigitado().length() > MAX_ESTADO_CIVIL) {
            return "O estado civil pode ter no máximo " + MAX_ESTADO_CIVIL
                    + " caracteres.";
        }
        LocalDate nascimento = campoNascimento.getValue();
        if (nascimento != null && !nascimento.isBefore(LocalDate.now())) {
            return "A data de nascimento deve estar no passado.";
        }
        if (lerInteiro(campoDependentes.getText()) < 0) {
            return "Dependentes deve ser um número inteiro, 0 ou mais.";
        }
        BigDecimal salario = lerValor(campoSalario.getText());
        if (salario != null && (salario.signum() < 0 || salario.scale() > 2)) {
            return "Informe o salário como 2500,00 (sem valor negativo).";
        }
        if (emEdicao == null) {
            String email = campoEmail.getText();
            if (!Validador.isEmailValido(email)) {
                return "Informe um e-mail de login válido.";
            }
            if (email.trim().length() > MAX_EMAIL) {
                return "O e-mail deve ter no máximo " + MAX_EMAIL
                        + " caracteres.";
            }
            String senha = campoSenha.getText();
            if (senha == null || senha.length() < MIN_SENHA) {
                return "A senha inicial deve ter pelo menos " + MIN_SENHA
                        + " caracteres.";
            }
        }
        return null;
    }
}
