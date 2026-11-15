package com.lojaroupas.controller;

import com.lojaroupas.model.Cliente;
import com.lojaroupas.model.Funcionario;
import com.lojaroupas.model.Locacao;
import com.lojaroupas.model.Peca;
import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.Configuracao;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Formatador;
import com.lojaroupas.util.Sessao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;

/**
 * Controller da tela de locações: lista das reservas à esquerda e
 * formulário de nova locação à direita.
 *
 * <p>O bloqueio de conflito de datas (RF-04, RN-04) é feito pela API: se
 * alguma peça já estiver reservada em período que se sobrepõe, a reserva
 * é recusada e a mensagem da API aparece no formulário.</p>
 */
public class LocacoesController {

    /**
     * Dados carregados de uma vez ao abrir a tela.
     *
     * @param clientes clientes cadastrados
     * @param pecas peças do acervo
     * @param funcionarios funcionários ativos
     * @param locacoes locações registradas
     */
    private record Dados(List<Cliente> clientes, List<Peca> pecas,
            List<Funcionario> funcionarios, List<Locacao> locacoes) {
    }

    /** Tabela de locações. */
    @FXML
    private TableView<Locacao> tabelaLocacoes;

    /** Coluna do cliente. */
    @FXML
    private TableColumn<Locacao, String> colunaCliente;

    /** Coluna das peças. */
    @FXML
    private TableColumn<Locacao, String> colunaPecas;

    /** Coluna da data do evento. */
    @FXML
    private TableColumn<Locacao, LocalDate> colunaEvento;

    /** Coluna da data de devolução. */
    @FXML
    private TableColumn<Locacao, LocalDate> colunaDevolucao;

    /** Coluna do atendente. */
    @FXML
    private TableColumn<Locacao, String> colunaAtendente;

    /** Botão de cancelar a reserva selecionada. */
    @FXML
    private Button botaoCancelar;

    /** Seleção do cliente. */
    @FXML
    private ComboBox<Cliente> campoCliente;

    /** Seleção do atendente. */
    @FXML
    private ComboBox<Funcionario> campoAtendente;

    /** Data do evento. */
    @FXML
    private DatePicker campoEvento;

    /** Data de devolução. */
    @FXML
    private DatePicker campoDevolucao;

    /** Lista de peças com caixas de marcação. */
    @FXML
    private ListView<Peca> listaPecas;

    /** Soma dos preços das peças marcadas. */
    @FXML
    private Label rotuloTotal;

    /** Mensagens de erro do formulário (inclusive conflito de datas). */
    @FXML
    private Label rotuloErro;

    /** Botão de registrar a locação. */
    @FXML
    private Button botaoRegistrar;

    /** Locações exibidas na tabela. */
    private final ObservableList<Locacao> locacoes =
            FXCollections.observableArrayList();

    /** Marcação de cada peça na lista, pelo identificador. */
    private final Map<Integer, BooleanProperty> marcadas = new HashMap<>();

    /** Nome do cliente pelo identificador, para a tabela. */
    private Map<Integer, String> nomesClientes = Map.of();

    /** Código da peça pelo identificador, para a tabela. */
    private Map<Integer, String> codigosPecas = Map.of();

    /** Nome do atendente pelo identificador, para a tabela. */
    private Map<Integer, String> nomesAtendentes = Map.of();

    /** Configura tabela, campos e lista de peças e carrega os dados. */
    @FXML
    private void initialize() {
        configurarTabela();
        configurarCampos();
        botaoCancelar.setDisable(true);
        tabelaLocacoes.getSelectionModel().selectedItemProperty()
                .addListener((observado, anterior, selecionada) ->
                        botaoCancelar.setDisable(selecionada == null));
        carregar();
    }

    /** Busca clientes, peças, atendentes e locações na API. */
    @FXML
    private void carregar() {
        ExecutorAssincrono.executar(
                () -> new Dados(
                        FabricaServicos.obterClienteService().listar(),
                        FabricaServicos.obterPecaService().listar(),
                        FabricaServicos.obterFuncionarioService()
                                .listarAtivos(),
                        FabricaServicos.obterLocacaoService().listar()),
                this::exibir,
                erro -> Alertas.exibirErro(
                        "Não foi possível carregar as locações",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Valida o formulário e registra a locação. */
    @FXML
    private void registrar() {
        String problema = validarFormulario();
        if (problema != null) {
            rotuloErro.setText(problema);
            return;
        }
        rotuloErro.setText("");

        Locacao nova = new Locacao(null,
                campoCliente.getValue().getIdCliente(),
                Sessao.getUsuarioLogado().getIdUsuario(),
                campoAtendente.getValue().idFuncionario(),
                campoEvento.getValue(), campoDevolucao.getValue(),
                pecasMarcadas().stream().map(Peca::idPeca).toList());

        botaoRegistrar.setDisable(true);
        ExecutorAssincrono.executar(
                () -> FabricaServicos.obterLocacaoService().criar(nova),
                gravada -> {
                    botaoRegistrar.setDisable(false);
                    Alertas.mostrarAviso("Locação registrada.");
                    limpar();
                    carregar();
                },
                erro -> {
                    botaoRegistrar.setDisable(false);
                    rotuloErro.setText(
                            ExecutorAssincrono.obterMensagem(erro));
                });
    }

    /** Limpa o formulário, mantendo o atendente escolhido. */
    @FXML
    private void limpar() {
        campoCliente.setValue(null);
        campoEvento.setValue(null);
        campoDevolucao.setValue(null);
        marcadas.values().forEach(marca -> marca.set(false));
        rotuloErro.setText("");
        atualizarTotal();
    }

    /** Cancela a reserva selecionada na tabela, após confirmação. */
    @FXML
    private void cancelar() {
        Locacao selecionada =
                tabelaLocacoes.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            return;
        }
        String pergunta = "Cancelar a reserva de "
                + nomesClientes.getOrDefault(selecionada.idCliente(),
                        "cliente")
                + " para " + Formatador.formatarData(
                        selecionada.dataEvento()) + "?";
        if (!Alertas.confirmar("Cancelar reserva", pergunta)) {
            return;
        }
        ExecutorAssincrono.executarSemRetorno(
                () -> FabricaServicos.obterLocacaoService()
                        .excluir(selecionada.idLocacao()),
                () -> {
                    Alertas.mostrarAviso("Reserva cancelada.");
                    carregar();
                },
                erro -> Alertas.exibirErro("Não foi possível cancelar",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /**
     * Preenche a tela com os dados carregados.
     *
     * @param dados clientes, peças, atendentes e locações
     */
    private void exibir(Dados dados) {
        nomesClientes = dados.clientes().stream().collect(
                Collectors.toMap(Cliente::getIdCliente, Cliente::getNome,
                        (a, b) -> a));
        codigosPecas = dados.pecas().stream().collect(
                Collectors.toMap(Peca::idPeca, Peca::codigoUnico,
                        (a, b) -> a));
        nomesAtendentes = dados.funcionarios().stream().collect(
                Collectors.toMap(Funcionario::idFuncionario,
                        Funcionario::nome, (a, b) -> a));

        Cliente clienteEscolhido = campoCliente.getValue();
        List<Cliente> clientes = new ArrayList<>(dados.clientes());
        clientes.sort(Comparator.comparing(Cliente::getNome,
                String.CASE_INSENSITIVE_ORDER));
        campoCliente.getItems().setAll(clientes);
        if (clienteEscolhido != null) {
            clientes.stream().filter(c -> Objects.equals(
                    c.getIdCliente(), clienteEscolhido.getIdCliente()))
                    .findFirst().ifPresent(campoCliente::setValue);
        }

        campoAtendente.getItems().setAll(dados.funcionarios());
        if (campoAtendente.getValue() == null) {
            Integer idUsuario = Sessao.getUsuarioLogado().getIdUsuario();
            dados.funcionarios().stream()
                    .filter(f -> idUsuario != null
                            && idUsuario.equals(f.idUsuario()))
                    .findFirst().ifPresent(campoAtendente::setValue);
        }

        for (Peca peca : dados.pecas()) {
            marcadas.computeIfAbsent(peca.idPeca(), id -> {
                BooleanProperty marca = new SimpleBooleanProperty(false);
                marca.addListener((o, antes, agora) -> atualizarTotal());
                return marca;
            });
        }
        listaPecas.getItems().setAll(dados.pecas());

        List<Locacao> ordenadas = new ArrayList<>(dados.locacoes());
        ordenadas.sort(Comparator.comparing(Locacao::dataEvento,
                Comparator.nullsLast(Comparator.reverseOrder())));
        locacoes.setAll(ordenadas);
        atualizarTotal();
    }

    /** Liga as colunas da tabela aos dados. */
    private void configurarTabela() {
        colunaCliente.setCellValueFactory(dados -> new ReadOnlyStringWrapper(
                nomesClientes.getOrDefault(dados.getValue().idCliente(),
                        "Cliente " + dados.getValue().idCliente())));
        colunaPecas.setCellValueFactory(dados -> new ReadOnlyStringWrapper(
                dados.getValue().idsPecas().stream()
                        .map(id -> codigosPecas.getOrDefault(id,
                                "Peça " + id))
                        .collect(Collectors.joining(", "))));
        colunaAtendente.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(nomesAtendentes.getOrDefault(
                        dados.getValue().idFuncionario(), "")));
        colunaEvento.setCellValueFactory(dados ->
                new ReadOnlyObjectWrapper<>(dados.getValue().dataEvento()));
        colunaDevolucao.setCellValueFactory(dados ->
                new ReadOnlyObjectWrapper<>(
                        dados.getValue().dataDevolucao()));
        colunaEvento.setCellFactory(coluna -> celulaData());
        colunaDevolucao.setCellFactory(coluna -> celulaData());

        tabelaLocacoes.setItems(locacoes);
        tabelaLocacoes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaLocacoes.setPlaceholder(
                new Label("Nenhuma locação registrada."));
    }

    /**
     * Cria uma célula de tabela que mostra a data como dd/mm/aaaa.
     *
     * @return célula de data
     */
    private TableCell<Locacao, LocalDate> celulaData() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setText(vazio ? null : Formatador.formatarData(data));
            }
        };
    }

    /** Configura as caixas de seleção, as datas e a lista de peças. */
    private void configurarCampos() {
        campoCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente == null ? ""
                        : cliente.getNome() + " · " + cliente.getCpf();
            }

            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        });
        campoEvento.valueProperty().addListener((o, antes, evento) -> {
            if (evento != null && campoDevolucao.getValue() == null) {
                campoDevolucao.setValue(evento.plusDays(2));
            }
        });
        listaPecas.setCellFactory(CheckBoxListCell.forListView(
                peca -> marcadas.computeIfAbsent(peca.idPeca(),
                        id -> new SimpleBooleanProperty(false)),
                new StringConverter<>() {
                    @Override
                    public String toString(Peca peca) {
                        return peca == null ? "" : peca.codigoUnico()
                                + "  " + Formatador.formatarMoeda(
                                        peca.preco())
                                + "  ·  " + peca.descricao()
                                + " (" + peca.tamanho() + ")";
                    }

                    @Override
                    public Peca fromString(String texto) {
                        return null;
                    }
                }));
        listaPecas.setPlaceholder(new Label("Nenhuma peça cadastrada."));
    }

    /**
     * Retorna as peças marcadas na lista.
     *
     * @return peças marcadas, na ordem da lista
     */
    private List<Peca> pecasMarcadas() {
        return listaPecas.getItems().stream()
                .filter(peca -> marcadas.containsKey(peca.idPeca())
                        && marcadas.get(peca.idPeca()).get())
                .toList();
    }

    /** Atualiza o total do aluguel com as peças marcadas. */
    private void atualizarTotal() {
        BigDecimal total = pecasMarcadas().stream().map(Peca::preco)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        rotuloTotal.setText(Formatador.formatarMoeda(total));
    }

    /**
     * Confere os campos do formulário.
     *
     * @return mensagem do primeiro problema, ou {@code null} se válido
     */
    private String validarFormulario() {
        if (campoCliente.getValue() == null) {
            return "Escolha o cliente.";
        }
        if (campoAtendente.getValue() == null) {
            return "Escolha o atendente.";
        }
        LocalDate evento = campoEvento.getValue();
        LocalDate devolucao = campoDevolucao.getValue();
        if (evento == null || devolucao == null) {
            return "Informe a data do evento e a de devolução.";
        }
        if (evento.isBefore(LocalDate.now())) {
            return "A data do evento não pode ser anterior a hoje.";
        }
        if (devolucao.isBefore(evento)) {
            return "A devolução não pode ser antes do evento.";
        }
        if (pecasMarcadas().isEmpty()) {
            return "Marque pelo menos uma peça.";
        }
        if (Sessao.getUsuarioLogado().getIdUsuario() == null
                && !Configuracao.isModoMock()) {
            return "Usuário sem identificação. Saia e entre de novo.";
        }
        return null;
    }
}
