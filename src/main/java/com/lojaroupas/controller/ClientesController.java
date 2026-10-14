package com.lojaroupas.controller;

import com.lojaroupas.model.Cliente;
import com.lojaroupas.service.ClienteService;
import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Formatador;
import com.lojaroupas.util.Sessao;
import com.lojaroupas.util.Validador;
import java.time.LocalDate;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller da tela de clientes: lista com busca à esquerda e
 * formulário à direita. Serve de modelo para as demais telas de cadastro.
 */
public class ClientesController {

    /** Tamanho máximo do nome, igual ao limite da API. */
    private static final int TAMANHO_MAXIMO_NOME = 60;

    /** Título dos diálogos desta tela. */
    private static final String TITULO_TELA = "Clientes";

    /** Título do formulário em modo de cadastro. */
    private static final String TITULO_NOVO = "Novo cliente";

    /** Título do formulário em modo de edição. */
    private static final String TITULO_EDICAO = "Editando cliente";

    /** Campo de busca da lista. */
    @FXML
    private TextField campoBusca;

    /** Tabela de clientes. */
    @FXML
    private TableView<Cliente> tabelaClientes;

    /** Coluna do nome. */
    @FXML
    private TableColumn<Cliente, String> colunaNome;

    /** Coluna do CPF. */
    @FXML
    private TableColumn<Cliente, String> colunaCpf;

    /** Coluna do RG. */
    @FXML
    private TableColumn<Cliente, String> colunaRg;

    /** Coluna da data de nascimento. */
    @FXML
    private TableColumn<Cliente, LocalDate> colunaNascimento;

    /** Título do formulário. */
    @FXML
    private Label rotuloTituloFormulario;

    /** Campo do nome. */
    @FXML
    private TextField campoNome;

    /** Campo do CPF. */
    @FXML
    private TextField campoCpf;

    /** Campo do RG. */
    @FXML
    private TextField campoRg;

    /** Seletor da data de nascimento. */
    @FXML
    private DatePicker campoNascimento;

    /** Campo das medidas. */
    @FXML
    private TextArea campoMedidas;

    /** Rótulo das mensagens de erro do formulário. */
    @FXML
    private Label rotuloErro;

    /** Botão de salvar. */
    @FXML
    private Button botaoSalvar;

    /** Botão de excluir. */
    @FXML
    private Button botaoExcluir;

    /** Serviço de acesso aos clientes. */
    private final ClienteService servico =
            FabricaServicos.obterClienteService();

    /** Clientes carregados do serviço. */
    private final ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

    /** Visão filtrada dos clientes, controlada pelo campo de busca. */
    private FilteredList<Cliente> clientesFiltrados;

    /** Cliente em edição; {@code null} quando é um cadastro novo. */
    private Cliente clienteEmEdicao;

    /** Configura tabela, busca e permissões e carrega a lista. */
    @FXML
    private void initialize() {
        configurarTabela();
        configurarBusca();

        // DECISÃO PENDENTE com o grupo: o funcionário pode excluir?
        // Por ora só o administrador. Para liberar, remova as duas linhas.
        botaoExcluir.setVisible(Sessao.isAdministrador());
        botaoExcluir.setManaged(Sessao.isAdministrador());

        novo();
        carregar();
    }

    /** Limpa o formulário para um novo cadastro. */
    @FXML
    private void novo() {
        clienteEmEdicao = null;
        tabelaClientes.getSelectionModel().clearSelection();
        campoNome.clear();
        campoCpf.clear();
        campoRg.clear();
        campoNascimento.setValue(null);
        campoMedidas.clear();
        rotuloErro.setText("");
        rotuloTituloFormulario.setText(TITULO_NOVO);
        botaoExcluir.setDisable(true);
    }

    /** Valida o formulário e salva o cliente (novo ou existente). */
    @FXML
    private void salvar() {
        String problema = validarFormulario();
        if (problema != null) {
            rotuloErro.setText(problema);
            return;
        }
        rotuloErro.setText("");

        Cliente cliente = new Cliente();
        cliente.setIdCliente(clienteEmEdicao == null
                ? null : clienteEmEdicao.getIdCliente());
        cliente.setNome(campoNome.getText().trim());
        cliente.setCpf(Formatador.formatarCpf(campoCpf.getText()));
        cliente.setRg(campoRg.getText().trim());
        cliente.setDataNascimento(campoNascimento.getValue());
        cliente.setMedidas(campoMedidas.getText().trim());

        botaoSalvar.setDisable(true);
        ExecutorAssincrono.executar(
                () -> servico.salvar(cliente),
                salvo -> {
                    botaoSalvar.setDisable(false);
                    Alertas.exibirInformacao(TITULO_TELA,
                            "Cliente salvo com sucesso.");
                    novo();
                    carregar();
                },
                erro -> {
                    botaoSalvar.setDisable(false);
                    rotuloErro.setText(
                            ExecutorAssincrono.obterMensagem(erro));
                });
    }

    /** Exclui o cliente em edição, após confirmação. */
    @FXML
    private void excluir() {
        if (clienteEmEdicao == null) {
            return;
        }
        Integer idCliente = clienteEmEdicao.getIdCliente();
        String pergunta = "Tem certeza que deseja excluir \""
                + clienteEmEdicao.getNome() + "\"?";
        if (!Alertas.confirmar("Excluir cliente", pergunta)) {
            return;
        }
        ExecutorAssincrono.executarSemRetorno(
                () -> servico.excluir(idCliente),
                () -> {
                    novo();
                    carregar();
                },
                erro -> Alertas.exibirErro("Não foi possível excluir",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Liga as colunas aos dados e cria a cadeia de filtro e ordenação. */
    private void configurarTabela() {
        colunaNome.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(dados.getValue().getNome()));
        colunaCpf.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(dados.getValue().getCpf()));
        colunaRg.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(dados.getValue().getRg()));
        colunaNascimento.setCellValueFactory(dados ->
                new ReadOnlyObjectWrapper<>(
                        dados.getValue().getDataNascimento()));
        colunaNascimento.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setText(vazio ? null : Formatador.formatarData(data));
            }
        });

        clientesFiltrados = new FilteredList<>(clientes, cliente -> true);
        SortedList<Cliente> ordenados = new SortedList<>(clientesFiltrados);
        ordenados.comparatorProperty().bind(
                tabelaClientes.comparatorProperty());
        tabelaClientes.setItems(ordenados);
        tabelaClientes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaClientes.setPlaceholder(
                new Label("Nenhum cliente encontrado."));

        tabelaClientes.getSelectionModel().selectedItemProperty()
                .addListener((observado, anterior, selecionado) -> {
                    if (selecionado != null) {
                        editar(selecionado);
                    }
                });
    }

    /** Aplica o filtro sempre que o texto de busca mudar. */
    private void configurarBusca() {
        campoBusca.textProperty().addListener(
                (observado, anterior, novo) -> aplicarFiltro(novo));
    }

    /** Recarrega a lista de clientes a partir do serviço. */
    private void carregar() {
        ExecutorAssincrono.executar(
                servico::listar,
                lista -> clientes.setAll(lista),
                erro -> Alertas.exibirErro(
                        "Não foi possível carregar os clientes",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /**
     * Carrega um cliente no formulário para edição.
     *
     * @param cliente cliente selecionado na tabela
     */
    private void editar(Cliente cliente) {
        clienteEmEdicao = cliente;
        campoNome.setText(cliente.getNome());
        campoCpf.setText(cliente.getCpf());
        campoRg.setText(cliente.getRg());
        campoNascimento.setValue(cliente.getDataNascimento());
        campoMedidas.setText(cliente.getMedidas());
        rotuloErro.setText("");
        rotuloTituloFormulario.setText(TITULO_EDICAO);
        botaoExcluir.setDisable(false);
    }

    /**
     * Confere os campos do formulário.
     *
     * @return mensagem do primeiro problema, ou {@code null} se válido
     */
    private String validarFormulario() {
        String nome = campoNome.getText();
        if (Validador.isVazio(nome)) {
            return "Informe o nome do cliente.";
        }
        if (nome.trim().length() > TAMANHO_MAXIMO_NOME) {
            return "O nome deve ter no máximo " + TAMANHO_MAXIMO_NOME
                    + " caracteres.";
        }
        if (!Validador.isCpfValido(campoCpf.getText())) {
            return "CPF inválido.";
        }
        LocalDate nascimento = campoNascimento.getValue();
        if (nascimento != null && nascimento.isAfter(LocalDate.now())) {
            return "A data de nascimento não pode ser futura.";
        }
        return null;
    }

    /**
     * Filtra a lista por nome, CPF ou RG.
     *
     * @param texto texto digitado no campo de busca
     */
    private void aplicarFiltro(String texto) {
        String termo = texto == null ? "" : texto.trim().toLowerCase();
        String digitos = Formatador.somenteDigitos(termo);
        clientesFiltrados.setPredicate(cliente -> termo.isEmpty()
                || corresponde(cliente, termo, digitos));
    }

    /**
     * Verifica se um cliente atende ao termo de busca.
     *
     * @param cliente cliente avaliado
     * @param termo texto de busca em minúsculas
     * @param digitos somente os dígitos do termo de busca
     * @return {@code true} se o cliente deve aparecer na lista
     */
    private boolean corresponde(Cliente cliente, String termo,
            String digitos) {
        boolean porNome = cliente.getNome().toLowerCase().contains(termo);
        String cpf = Formatador.somenteDigitos(cliente.getCpf());
        String rg = Formatador.somenteDigitos(cliente.getRg());
        boolean porNumero = !digitos.isEmpty()
                && (cpf.contains(digitos) || rg.contains(digitos));
        return porNome || porNumero;
    }
}