package com.lojaroupas.controller;

import com.lojaroupas.model.ResumoFinanceiro;
import com.lojaroupas.model.ResumoFinanceiro.DesempenhoFuncionario;
import com.lojaroupas.model.ResumoFinanceiro.DesempenhoPeca;
import com.lojaroupas.model.ResumoFinanceiro.ReceitaMensal;
import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Formatador;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller da tela Financeiro (somente administrador).
 *
 * <p>Mostra uma ESTIMATIVA de receita calculada a partir das locações e
 * do preço atual das peças: indicadores, gráfico de receita por mês e
 * tabelas por funcionário e por peça. Os valores reais (sinal e
 * pagamentos) dependem de um módulo que o back-end ainda não tem.</p>
 */
public class FinanceiroController {

    /** Formato do mês no gráfico, como em 01/2026. */
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MM/yyyy");

    /** Alinhamento à direita das colunas de números e valores. */
    private static final String ESTILO_DIREITA =
            "-fx-alignment: CENTER-RIGHT;";

    /** Períodos que o usuário pode escolher na tela. */
    private enum Periodo {

        /** Sem limite de datas. */
        TODO_O_PERIODO("Todo o período"),

        /** Do primeiro ao último dia do mês atual. */
        ESTE_MES("Este mês"),

        /** O mês atual e os dois anteriores. */
        ULTIMOS_3_MESES("Últimos 3 meses (com o atual)"),

        /** De 1º de janeiro a 31 de dezembro do ano atual. */
        ESTE_ANO("Este ano");

        /** Texto exibido na lista de períodos. */
        private final String rotulo;

        /**
         * Cria um período.
         *
         * @param rotulo texto exibido na lista
         */
        Periodo(String rotulo) {
            this.rotulo = rotulo;
        }

        /**
         * Primeiro dia do período.
         *
         * @param hoje data de hoje
         * @return primeiro dia (inclusive), ou {@code null} se sem limite
         */
        LocalDate inicio(LocalDate hoje) {
            return switch (this) {
                case TODO_O_PERIODO -> null;
                case ESTE_MES -> hoje.withDayOfMonth(1);
                case ULTIMOS_3_MESES ->
                        hoje.minusMonths(2).withDayOfMonth(1);
                case ESTE_ANO -> hoje.withDayOfYear(1);
            };
        }

        /**
         * Último dia do período.
         *
         * @param hoje data de hoje
         * @return último dia (inclusive), ou {@code null} se sem limite
         */
        LocalDate fim(LocalDate hoje) {
            return switch (this) {
                case TODO_O_PERIODO -> null;
                case ESTE_MES, ULTIMOS_3_MESES ->
                        hoje.with(TemporalAdjusters.lastDayOfMonth());
                case ESTE_ANO -> hoje.with(TemporalAdjusters.lastDayOfYear());
            };
        }

        /**
         * Texto exibido na lista de períodos.
         *
         * @return rótulo do período
         */
        @Override
        public String toString() {
            return rotulo;
        }
    }

    /** Lista de períodos. */
    @FXML
    private ComboBox<Periodo> comboPeriodo;

    /** Indicador da receita estimada. */
    @FXML
    private Label rotuloReceita;

    /** Indicador da quantidade de locações. */
    @FXML
    private Label rotuloLocacoes;

    /** Indicador do ticket médio. */
    @FXML
    private Label rotuloTicketMedio;

    /** Indicador da quantidade de peças locadas. */
    @FXML
    private Label rotuloPecas;

    /** Aviso de peças locadas sem preço. */
    @FXML
    private Label rotuloAvisoPrecos;

    /** Gráfico de receita por mês. */
    @FXML
    private BarChart<String, Number> graficoMeses;

    /** Tabela de desempenho por funcionário. */
    @FXML
    private TableView<DesempenhoFuncionario> tabelaFuncionarios;

    /** Coluna do nome do funcionário. */
    @FXML
    private TableColumn<DesempenhoFuncionario, String> colunaFuncionario;

    /** Coluna de locações do funcionário. */
    @FXML
    private TableColumn<DesempenhoFuncionario, String> colunaLocacoesFunc;

    /** Coluna da receita do funcionário. */
    @FXML
    private TableColumn<DesempenhoFuncionario, String> colunaReceitaFunc;

    /** Tabela das peças mais alugadas. */
    @FXML
    private TableView<DesempenhoPeca> tabelaPecas;

    /** Coluna do código da peça. */
    @FXML
    private TableColumn<DesempenhoPeca, String> colunaCodigo;

    /** Coluna da descrição da peça. */
    @FXML
    private TableColumn<DesempenhoPeca, String> colunaDescricao;

    /** Coluna de quantas vezes a peça foi locada. */
    @FXML
    private TableColumn<DesempenhoPeca, String> colunaVezes;

    /** Coluna da receita da peça. */
    @FXML
    private TableColumn<DesempenhoPeca, String> colunaReceitaPeca;

    /**
     * Número da última consulta feita. Serve para ignorar respostas
     * antigas quando o usuário troca de período rapidamente.
     */
    private int ultimaConsulta;

    /** Configura a lista de períodos, as tabelas e carrega os dados. */
    @FXML
    private void initialize() {
        comboPeriodo.setItems(
                FXCollections.observableArrayList(Periodo.values()));
        comboPeriodo.setValue(Periodo.TODO_O_PERIODO);
        comboPeriodo.valueProperty().addListener(
                (observado, anterior, novo) -> carregar());

        configurarTabelaFuncionarios();
        configurarTabelaPecas();
        carregar();
    }

    /** Consulta o resumo do período escolhido e atualiza a tela. */
    @FXML
    private void carregar() {
        Periodo periodo = comboPeriodo.getValue();
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = periodo.inicio(hoje);
        LocalDate fim = periodo.fim(hoje);
        int consulta = ++ultimaConsulta;

        ExecutorAssincrono.executar(
                () -> FabricaServicos.obterFinanceiroService()
                        .obterResumo(inicio, fim),
                resumo -> {
                    if (consulta == ultimaConsulta) {
                        exibir(resumo);
                    }
                },
                erro -> {
                    if (consulta == ultimaConsulta) {
                        Alertas.exibirErro(
                                "Não foi possível carregar o financeiro",
                                ExecutorAssincrono.obterMensagem(erro));
                    }
                });
    }

    /** Liga as colunas da tabela de funcionários aos dados. */
    private void configurarTabelaFuncionarios() {
        colunaFuncionario.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(linha.getValue().nome()));
        colunaLocacoesFunc.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(
                        String.valueOf(linha.getValue().locacoes())));
        colunaReceitaFunc.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(
                        Formatador.formatarMoeda(linha.getValue().receita())));
        colunaLocacoesFunc.setStyle(ESTILO_DIREITA);
        colunaReceitaFunc.setStyle(ESTILO_DIREITA);
        tabelaFuncionarios.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaFuncionarios.setPlaceholder(
                new Label("Sem locações no período."));
    }

    /** Liga as colunas da tabela de peças aos dados. */
    private void configurarTabelaPecas() {
        colunaCodigo.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(linha.getValue().codigoUnico()));
        colunaDescricao.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(linha.getValue().descricao()));
        colunaVezes.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(
                        String.valueOf(linha.getValue().vezes())));
        colunaReceitaPeca.setCellValueFactory(linha ->
                new ReadOnlyStringWrapper(
                        Formatador.formatarMoeda(linha.getValue().receita())));
        colunaVezes.setStyle(ESTILO_DIREITA);
        colunaReceitaPeca.setStyle(ESTILO_DIREITA);
        tabelaPecas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaPecas.setPlaceholder(new Label("Sem locações no período."));
    }

    /**
     * Mostra o resumo nos indicadores, no gráfico e nas tabelas.
     *
     * @param resumo resumo financeiro estimado do período
     */
    private void exibir(ResumoFinanceiro resumo) {
        rotuloReceita.setText(
                Formatador.formatarMoeda(resumo.receitaEstimada()));
        rotuloLocacoes.setText(String.valueOf(resumo.totalLocacoes()));
        rotuloTicketMedio.setText(
                Formatador.formatarMoeda(resumo.ticketMedio()));
        rotuloPecas.setText(String.valueOf(resumo.totalPecasLocadas()));

        boolean haPecasSemPreco = resumo.pecasSemPreco() > 0;
        rotuloAvisoPrecos.setVisible(haPecasSemPreco);
        rotuloAvisoPrecos.setManaged(haPecasSemPreco);
        if (haPecasSemPreco) {
            rotuloAvisoPrecos.setText(resumo.pecasSemPreco()
                    + " peça(s) locada(s) sem preço no cadastro (ou já "
                    + "removida(s)) foram contadas como R$ 0,00.");
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (ReceitaMensal mes : resumo.porMes()) {
            serie.getData().add(new XYChart.Data<>(
                    FORMATO_MES.format(mes.mes()),
                    mes.receita().doubleValue()));
        }
        graficoMeses.getData().clear();
        graficoMeses.getData().add(serie);

        tabelaFuncionarios.setItems(
                FXCollections.observableArrayList(resumo.porFuncionario()));
        tabelaPecas.setItems(
                FXCollections.observableArrayList(resumo.pecasMaisAlugadas()));
    }
}
