package com.lojaroupas.controller;

import com.lojaroupas.model.Peca;
import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.service.PecaService;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Formatador;
import com.lojaroupas.util.Sessao;
import com.lojaroupas.util.Validador;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller da tela de peças do acervo: busca pela etiqueta, filtro,
 * lista e formulário de cadastro e edição (RF-02, RF-19, RF-20 e RN-11).
 */
public class PecasController {

    /** Limites de tamanho, iguais aos de {@code ProdutoRequest} na API. */
    private static final int MAX_CODIGO = 40;
    private static final int MAX_DESCRICAO = 100;
    private static final int MAX_TAMANHO = 10;
    private static final int MAX_COR = 30;
    private static final int MAX_MATERIAL = 40;

    /** Opção do filtro que mostra todos os tamanhos. */
    private static final String TODOS_TAMANHOS = "Todos";

    /** Tamanhos sugeridos no formulário (o campo aceita outros). */
    private static final List<String> TAMANHOS =
            List.of("PP", "P", "M", "G", "GG", "XG");

    /** Campo do código da etiqueta, para a busca direta. */
    @FXML
    private TextField campoCodigo;

    /** Campo de filtro por texto. */
    @FXML
    private TextField campoBusca;

    /** Filtro por tamanho. */
    @FXML
    private ComboBox<String> campoFiltroTamanho;

    /** Tabela de peças. */
    @FXML
    private TableView<Peca> tabelaPecas;

    /** Coluna do código. */
    @FXML
    private TableColumn<Peca, String> colunaCodigo;

    /** Coluna da descrição. */
    @FXML
    private TableColumn<Peca, String> colunaDescricao;

    /** Coluna do tamanho. */
    @FXML
    private TableColumn<Peca, String> colunaTamanho;

    /** Coluna da cor. */
    @FXML
    private TableColumn<Peca, String> colunaCor;

    /** Coluna do material. */
    @FXML
    private TableColumn<Peca, String> colunaMaterial;

    /** Coluna do valor do aluguel. */
    @FXML
    private TableColumn<Peca, BigDecimal> colunaPreco;

    /** Rótulo com a quantidade de peças exibidas. */
    @FXML
    private Label rotuloTotal;

    /** Título do formulário. */
    @FXML
    private Label rotuloTituloFormulario;

    /** Campo do código no formulário. */
    @FXML
    private TextField campoCodigoForm;

    /** Campo da descrição. */
    @FXML
    private TextField campoDescricao;

    /** Campo do tamanho (aceita valores fora da lista). */
    @FXML
    private ComboBox<String> campoTamanho;

    /** Campo da cor. */
    @FXML
    private TextField campoCor;

    /** Campo do material. */
    @FXML
    private TextField campoMaterial;

    /** Campo do valor do aluguel. */
    @FXML
    private TextField campoPreco;

    /** Rótulo das mensagens de erro do formulário. */
    @FXML
    private Label rotuloErro;

    /** Botão de salvar. */
    @FXML
    private Button botaoSalvar;

    /** Botão de excluir. */
    @FXML
    private Button botaoExcluir;

    /** Serviço de acesso às peças. */
    private final PecaService servico = FabricaServicos.obterPecaService();

    /** Peças carregadas do serviço. */
    private final ObservableList<Peca> pecas =
            FXCollections.observableArrayList();

    /** Visão filtrada das peças. */
    private FilteredList<Peca> pecasFiltradas;

    /** Peça em edição; {@code null} quando é um cadastro novo. */
    private Peca pecaEmEdicao;

    /** Configura tabela, filtros e permissões e carrega o acervo. */
    @FXML
    private void initialize() {
        configurarTabela();
        campoTamanho.getItems().setAll(TAMANHOS);
        campoBusca.textProperty().addListener((o, a, n) -> aplicarFiltro());
        campoFiltroTamanho.valueProperty()
                .addListener((o, a, n) -> aplicarFiltro());

        // Mesma regra da tela de clientes: só o administrador exclui.
        botaoExcluir.setVisible(Sessao.isAdministrador());
        botaoExcluir.setManaged(Sessao.isAdministrador());

        novo();
        carregar();
    }

    /** Recarrega o acervo a partir do serviço. */
    @FXML
    private void carregar() {
        ExecutorAssincrono.executar(
                servico::listar,
                lista -> {
                    pecas.setAll(lista);
                    atualizarFiltroTamanhos();
                    aplicarFiltro();
                },
                erro -> Alertas.exibirErro(
                        "Não foi possível carregar as peças",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Busca a peça pelo código da etiqueta e a abre no formulário. */
    @FXML
    private void buscarPorCodigo() {
        String codigo = campoCodigo.getText();
        if (Validador.isVazio(codigo)) {
            Alertas.exibirErro("Busca pela etiqueta",
                    "Digite o código da etiqueta, por exemplo TRJ-002.");
            return;
        }
        ExecutorAssincrono.executar(
                () -> servico.buscarPorCodigo(codigo),
                peca -> {
                    campoBusca.clear();
                    campoFiltroTamanho.setValue(TODOS_TAMANHOS);
                    pecas.stream()
                            .filter(p -> p.idPeca().equals(peca.idPeca()))
                            .findFirst()
                            .ifPresentOrElse(
                                    p -> {
                                        tabelaPecas.getSelectionModel().select(p);
                                        tabelaPecas.scrollTo(p);
                                    },
                                    () -> editar(peca));
                    Alertas.mostrarAviso("Peça encontrada: "
                            + peca.codigoUnico());
                },
                erro -> Alertas.exibirErro("Etiqueta não encontrada",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Limpa o formulário para um cadastro novo. */
    @FXML
    private void novo() {
        pecaEmEdicao = null;
        tabelaPecas.getSelectionModel().clearSelection();
        campoCodigoForm.clear();
        campoDescricao.clear();
        campoTamanho.setValue(null);
        campoTamanho.getEditor().clear();
        campoCor.clear();
        campoMaterial.clear();
        campoPreco.clear();
        rotuloErro.setText("");
        rotuloTituloFormulario.setText("Nova peça");
        botaoExcluir.setDisable(true);
    }

    /** Valida o formulário e salva a peça (nova ou existente). */
    @FXML
    private void salvar() {
        BigDecimal preco = lerPreco(campoPreco.getText());
        String problema = validar(preco);
        if (problema != null) {
            rotuloErro.setText(problema);
            return;
        }
        rotuloErro.setText("");
        Peca peca = new Peca(
                pecaEmEdicao == null ? null : pecaEmEdicao.idPeca(),
                campoCodigoForm.getText().trim().toUpperCase(Locale.ROOT),
                campoDescricao.getText().trim(),
                tamanhoDigitado(),
                campoCor.getText().trim(),
                campoMaterial.getText().trim(),
                preco);

        botaoSalvar.setDisable(true);
        ExecutorAssincrono.executar(
                () -> servico.salvar(peca),
                salva -> {
                    botaoSalvar.setDisable(false);
                    Alertas.mostrarAviso("Peça " + salva.codigoUnico()
                            + " salva com sucesso.");
                    novo();
                    carregar();
                },
                erro -> {
                    botaoSalvar.setDisable(false);
                    rotuloErro.setText(ExecutorAssincrono.obterMensagem(erro));
                });
    }

    /** Exclui a peça em edição, após confirmação. */
    @FXML
    private void excluir() {
        if (pecaEmEdicao == null) {
            return;
        }
        Integer idPeca = pecaEmEdicao.idPeca();
        String pergunta = "Tem certeza que deseja excluir a peça "
                + pecaEmEdicao.codigoUnico() + "?";
        if (!Alertas.confirmar("Excluir peça", pergunta)) {
            return;
        }
        ExecutorAssincrono.executarSemRetorno(
                () -> servico.excluir(idPeca),
                () -> {
                    novo();
                    carregar();
                },
                erro -> Alertas.exibirErro("Não foi possível excluir",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /** Liga as colunas aos dados e cria o filtro e a ordenação. */
    private void configurarTabela() {
        colunaCodigo.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().codigoUnico()));
        colunaDescricao.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().descricao()));
        colunaTamanho.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().tamanho()));
        colunaCor.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().cor()));
        colunaMaterial.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().material()));
        colunaPreco.setCellValueFactory(d ->
                new ReadOnlyObjectWrapper<>(d.getValue().preco()));
        colunaPreco.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean vazio) {
                super.updateItem(valor, vazio);
                setText(vazio || valor == null
                        ? null : Formatador.formatarMoeda(valor));
            }
        });

        pecasFiltradas = new FilteredList<>(pecas, p -> true);
        SortedList<Peca> ordenadas = new SortedList<>(pecasFiltradas);
        ordenadas.comparatorProperty().bind(tabelaPecas.comparatorProperty());
        tabelaPecas.setItems(ordenadas);
        tabelaPecas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaPecas.setPlaceholder(new Label("Nenhuma peça encontrada."));
        tabelaPecas.getSortOrder().add(colunaCodigo);
        tabelaPecas.getSelectionModel().selectedItemProperty()
                .addListener((o, anterior, selecionada) -> {
                    if (selecionada != null) {
                        editar(selecionada);
                    }
                });
    }

    /** Monta a lista do filtro de tamanho com os tamanhos do acervo. */
    private void atualizarFiltroTamanhos() {
        String atual = campoFiltroTamanho.getValue();
        List<String> tamanhos = pecas.stream().map(Peca::tamanho)
                .filter(t -> t != null && !t.isBlank())
                .distinct().sorted().toList();
        campoFiltroTamanho.getItems().setAll(TODOS_TAMANHOS);
        campoFiltroTamanho.getItems().addAll(tamanhos);
        campoFiltroTamanho.setValue(
                atual != null && campoFiltroTamanho.getItems().contains(atual)
                        ? atual : TODOS_TAMANHOS);
    }

    /** Aplica o filtro de texto e de tamanho e atualiza o total. */
    private void aplicarFiltro() {
        String termo = campoBusca.getText() == null
                ? "" : campoBusca.getText().trim().toLowerCase(Locale.ROOT);
        String tamanho = campoFiltroTamanho.getValue();
        pecasFiltradas.setPredicate(p ->
                (tamanho == null || TODOS_TAMANHOS.equals(tamanho)
                        || tamanho.equalsIgnoreCase(p.tamanho()))
                && (termo.isEmpty() || contem(p, termo)));
        rotuloTotal.setText(pecasFiltradas.size() + " de " + pecas.size()
                + " peças");
    }

    /**
     * Verifica se a peça contém o termo em algum campo de texto.
     *
     * @param p peça
     * @param termo texto em minúsculas
     * @return {@code true} se a peça deve aparecer
     */
    private boolean contem(Peca p, String termo) {
        for (String campo : new String[] {p.codigoUnico(), p.descricao(),
                p.cor(), p.material()}) {
            if (campo != null
                    && campo.toLowerCase(Locale.ROOT).contains(termo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Carrega uma peça no formulário para edição.
     *
     * @param peca peça selecionada
     */
    private void editar(Peca peca) {
        pecaEmEdicao = peca;
        campoCodigoForm.setText(peca.codigoUnico());
        campoDescricao.setText(peca.descricao());
        campoTamanho.setValue(peca.tamanho());
        campoTamanho.getEditor().setText(peca.tamanho());
        campoCor.setText(peca.cor());
        campoMaterial.setText(peca.material());
        campoPreco.setText(peca.preco() == null ? ""
                : peca.preco().toPlainString().replace('.', ','));
        rotuloErro.setText("");
        rotuloTituloFormulario.setText("Editando " + peca.codigoUnico());
        botaoExcluir.setDisable(false);
    }

    /**
     * Lê o tamanho digitado ou escolhido no campo editável.
     *
     * @return tamanho sem espaços, em maiúsculas
     */
    private String tamanhoDigitado() {
        String texto = campoTamanho.getEditor().getText();
        if (Validador.isVazio(texto) && campoTamanho.getValue() != null) {
            texto = campoTamanho.getValue();
        }
        return texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Converte o valor digitado (aceita vírgula ou ponto).
     *
     * @param texto valor digitado
     * @return valor, ou {@code null} se não for um número
     */
    private BigDecimal lerPreco(String texto) {
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
            return null;
        }
    }

    /**
     * Confere os campos do formulário.
     *
     * @param preco valor já convertido
     * @return mensagem do primeiro problema, ou {@code null} se válido
     */
    private String validar(BigDecimal preco) {
        String codigo = campoCodigoForm.getText();
        if (Validador.isVazio(codigo)) {
            return "Informe o código da etiqueta.";
        }
        if (codigo.trim().length() > MAX_CODIGO) {
            return "O código deve ter no máximo " + MAX_CODIGO + " caracteres.";
        }
        String descricao = campoDescricao.getText();
        if (Validador.isVazio(descricao)) {
            return "Informe a descrição da peça.";
        }
        if (descricao.trim().length() > MAX_DESCRICAO) {
            return "A descrição deve ter no máximo " + MAX_DESCRICAO
                    + " caracteres.";
        }
        String tamanho = tamanhoDigitado();
        if (tamanho.isEmpty()) {
            return "Informe o tamanho.";
        }
        if (tamanho.length() > MAX_TAMANHO) {
            return "O tamanho deve ter no máximo " + MAX_TAMANHO
                    + " caracteres.";
        }
        String cor = campoCor.getText();
        if (Validador.isVazio(cor)) {
            return "Informe a cor.";
        }
        if (cor.trim().length() > MAX_COR) {
            return "A cor deve ter no máximo " + MAX_COR + " caracteres.";
        }
        if (campoMaterial.getText() != null
                && campoMaterial.getText().trim().length() > MAX_MATERIAL) {
            return "O material deve ter no máximo " + MAX_MATERIAL
                    + " caracteres.";
        }
        if (preco == null) {
            return "Informe o valor do aluguel, por exemplo 250,00.";
        }
        if (preco.signum() < 0) {
            return "O valor do aluguel não pode ser negativo.";
        }
        if (preco.scale() > 2) {
            return "Use no máximo duas casas decimais no valor.";
        }
        return null;
    }
}
