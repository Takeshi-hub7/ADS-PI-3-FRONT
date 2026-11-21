package com.lojaroupas.controller;

import com.lojaroupas.model.Cliente;
import com.lojaroupas.model.Locacao;
import com.lojaroupas.model.Peca;
import com.lojaroupas.service.FabricaServicos;
import com.lojaroupas.util.Alertas;
import com.lojaroupas.util.ExecutorAssincrono;
import com.lojaroupas.util.Formatador;
import com.lojaroupas.util.Sessao;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller da tela inicial: resumo do dia com indicadores e a lista
 * dos próximos eventos, calculados a partir da data de hoje.
 */
public class InicioController {

    /** Janela, em dias, dos indicadores de eventos e devoluções. */
    private static final int DIAS_JANELA = 7;

    /** Formato da data por extenso no cabeçalho. */
    private static final DateTimeFormatter DATA_EXTENSO =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                    Locale.forLanguageTag("pt-BR"));

    /**
     * Dados carregados de uma vez na API.
     *
     * @param clientes clientes cadastrados
     * @param pecas peças do acervo
     * @param locacoes locações registradas
     */
    private record Dados(List<Cliente> clientes, List<Peca> pecas,
            List<Locacao> locacoes) {
    }

    /**
     * Linha da tabela de próximos eventos, já com os nomes resolvidos.
     *
     * @param locacao locação
     * @param cliente nome do cliente
     * @param pecas códigos e descrições das peças
     */
    private record Evento(Locacao locacao, String cliente, String pecas) {
    }

    /** Saudação com o nome do usuário. */
    @FXML
    private Label rotuloBoasVindas;

    /** Data de hoje por extenso. */
    @FXML
    private Label rotuloData;

    /** Indicador: eventos nos próximos dias. */
    @FXML
    private Label rotuloEventosSemana;

    /** Indicador: devoluções nos próximos dias. */
    @FXML
    private Label rotuloDevolucoesSemana;

    /** Indicador: peças no acervo. */
    @FXML
    private Label rotuloPecas;

    /** Detalhe do indicador de peças (quantas estão livres hoje). */
    @FXML
    private Label rotuloPecasDetalhe;

    /** Indicador: clientes cadastrados. */
    @FXML
    private Label rotuloClientes;

    /** Tabela de próximos eventos. */
    @FXML
    private TableView<Evento> tabelaEventos;

    /** Coluna "daqui a quanto tempo". */
    @FXML
    private TableColumn<Evento, String> colunaQuando;

    /** Coluna da data do evento. */
    @FXML
    private TableColumn<Evento, String> colunaEvento;

    /** Coluna do cliente. */
    @FXML
    private TableColumn<Evento, String> colunaCliente;

    /** Coluna das peças. */
    @FXML
    private TableColumn<Evento, String> colunaPecas;

    /** Coluna da devolução. */
    @FXML
    private TableColumn<Evento, String> colunaDevolucao;

    /** Preenche o cabeçalho, configura a tabela e carrega os dados. */
    @FXML
    private void initialize() {
        String nome = Sessao.getUsuarioLogado().getNome();
        rotuloBoasVindas.setText("Olá, " + nome.split(" ")[0]);
        String data = LocalDate.now().format(DATA_EXTENSO);
        rotuloData.setText(data.substring(0, 1).toUpperCase() + data.substring(1));

        colunaQuando.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                quando(d.getValue().locacao().dataEvento())));
        colunaEvento.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                Formatador.formatarData(d.getValue().locacao().dataEvento())));
        colunaCliente.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().cliente()));
        colunaPecas.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(d.getValue().pecas()));
        colunaDevolucao.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().locacao().dataDevolucao() == null ? "–"
                        : Formatador.formatarData(
                                d.getValue().locacao().dataDevolucao())));
        tabelaEventos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabelaEventos.setPlaceholder(new Label("Nenhum evento agendado."));

        carregar();
    }

    /** Busca clientes, peças e locações e atualiza o resumo. */
    @FXML
    private void carregar() {
        ExecutorAssincrono.executar(
                () -> new Dados(
                        FabricaServicos.obterClienteService().listar(),
                        FabricaServicos.obterPecaService().listar(),
                        FabricaServicos.obterLocacaoService().listar()),
                this::exibir,
                erro -> Alertas.exibirErro(
                        "Não foi possível carregar o resumo",
                        ExecutorAssincrono.obterMensagem(erro)));
    }

    /**
     * Calcula os indicadores e monta a lista de próximos eventos.
     *
     * @param dados clientes, peças e locações
     */
    private void exibir(Dados dados) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_JANELA);

        long eventos = dados.locacoes().stream()
                .filter(l -> dentro(l.dataEvento(), hoje, limite)).count();
        long devolucoes = dados.locacoes().stream()
                .filter(l -> dentro(l.dataDevolucao(), hoje, limite)).count();
        long ocupadas = dados.locacoes().stream()
                .filter(l -> ocupadaHoje(l, hoje))
                .flatMap(l -> l.idsPecas().stream()).distinct().count();

        rotuloEventosSemana.setText(String.valueOf(eventos));
        rotuloDevolucoesSemana.setText(String.valueOf(devolucoes));
        rotuloPecas.setText(String.valueOf(dados.pecas().size()));
        rotuloPecasDetalhe.setText("Peças no acervo · "
                + (dados.pecas().size() - ocupadas) + " livres hoje");
        rotuloClientes.setText(String.valueOf(dados.clientes().size()));

        Map<Integer, String> clientes = dados.clientes().stream().collect(
                Collectors.toMap(Cliente::getIdCliente, Cliente::getNome,
                        (a, b) -> a));
        Map<Integer, Peca> pecas = dados.pecas().stream().collect(
                Collectors.toMap(Peca::idPeca, Function.identity(),
                        (a, b) -> a));
        tabelaEventos.getItems().setAll(dados.locacoes().stream()
                .filter(l -> l.dataEvento() != null
                        && !l.dataEvento().isBefore(hoje))
                .sorted(Comparator.comparing(Locacao::dataEvento))
                .map(l -> new Evento(l,
                        clientes.getOrDefault(l.idCliente(), "–"),
                        l.idsPecas().stream().map(pecas::get)
                                .filter(Objects::nonNull)
                                .map(p -> p.codigoUnico() + " " + p.descricao())
                                .collect(Collectors.joining(", "))))
                .toList());
    }

    /**
     * Indica se a data está entre hoje e o limite, inclusive.
     *
     * @param data data avaliada
     * @param hoje início da janela
     * @param limite fim da janela
     * @return {@code true} se está dentro
     */
    private boolean dentro(LocalDate data, LocalDate hoje, LocalDate limite) {
        return data != null && !data.isBefore(hoje) && !data.isAfter(limite);
    }

    /**
     * Indica se as peças da locação estão fora da loja hoje, do evento
     * à devolução (mesmo período usado no bloqueio de conflitos).
     *
     * @param l locação
     * @param hoje data de hoje
     * @return {@code true} se a locação ocupa as peças hoje
     */
    private boolean ocupadaHoje(Locacao l, LocalDate hoje) {
        LocalDate fim = l.dataDevolucao() == null
                ? l.dataEvento() : l.dataDevolucao();
        return l.dataEvento() != null && !hoje.isBefore(l.dataEvento())
                && !hoje.isAfter(fim);
    }

    /**
     * Texto curto de quanto falta para o evento.
     *
     * @param data data do evento
     * @return "Hoje", "Amanhã" ou "em N dias"
     */
    private String quando(LocalDate data) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), data);
        if (dias == 0) {
            return "Hoje";
        }
        return dias == 1 ? "Amanhã" : "em " + dias + " dias";
    }
}
