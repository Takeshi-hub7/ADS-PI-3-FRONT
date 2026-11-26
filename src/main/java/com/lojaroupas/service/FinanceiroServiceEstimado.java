package com.lojaroupas.service;

import com.lojaroupas.model.Funcionario;
import com.lojaroupas.model.Locacao;
import com.lojaroupas.model.Peca;
import com.lojaroupas.model.ResumoFinanceiro;
import com.lojaroupas.model.ResumoFinanceiro.DesempenhoFuncionario;
import com.lojaroupas.model.ResumoFinanceiro.DesempenhoPeca;
import com.lojaroupas.model.ResumoFinanceiro.ReceitaMensal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Resumo financeiro ESTIMADO a partir das locações.
 *
 * <p>Regra: cada locação vale a soma do preço ATUAL das peças que ela
 * contém, e é atribuída ao mês da data do evento. Isso é uma aproximação:
 * se o preço de uma peça mudar depois da locação, o valor estimado muda
 * junto. O valor real (preço gravado e pagamentos) depende de módulos
 * que o back-end ainda não oferece.</p>
 *
 * <p>Funciona igual nos modos mock e API, porque usa apenas os serviços de
 * locação, peça e funcionário.</p>
 */
public class FinanceiroServiceEstimado implements FinanceiroService {

    /** Quantidade máxima de peças na lista das mais alugadas. */
    private static final int LIMITE_PECAS_MAIS_ALUGADAS = 10;

    /** Casas decimais dos valores em reais. */
    private static final int ESCALA_MONETARIA = 2;

    /** Código exibido para uma peça que não existe mais no cadastro. */
    private static final String CODIGO_PECA_REMOVIDA = "—";

    /** Nome exibido quando a locação não informa o funcionário. */
    private static final String FUNCIONARIO_NAO_INFORMADO =
            "Não informado";

    /** Serviço de onde saem as locações. */
    private final LocacaoService locacaoService;

    /** Serviço de onde saem os preços das peças. */
    private final PecaService pecaService;

    /** Serviço de onde saem os nomes dos funcionários. */
    private final FuncionarioService funcionarioService;

    /**
     * Cria o serviço.
     *
     * @param locacaoService serviço de locações
     * @param pecaService serviço de peças
     * @param funcionarioService serviço de funcionários
     */
    public FinanceiroServiceEstimado(LocacaoService locacaoService,
            PecaService pecaService, FuncionarioService funcionarioService) {
        this.locacaoService = locacaoService;
        this.pecaService = pecaService;
        this.funcionarioService = funcionarioService;
    }

    /**
     * Carrega os dados e calcula o resumo do período.
     *
     * @param inicio primeiro dia do período (inclusive), ou {@code null}
     * @param fim último dia do período (inclusive), ou {@code null}
     * @return resumo estimado do período
     */
    @Override
    public ResumoFinanceiro obterResumo(LocalDate inicio, LocalDate fim) {
        List<Locacao> locacoes = locacaoService.listar();
        List<Peca> pecas = pecaService.listar();
        List<Funcionario> funcionarios = funcionarioService.listarTodos();
        return calcular(locacoes, pecas, funcionarios, inicio, fim);
    }

    /**
     * Calcula o resumo a partir de listas já carregadas. É uma função
     * pura, sem acesso a rede, o que facilita os testes.
     *
     * @param locacoes todas as locações
     * @param pecas todas as peças do cadastro
     * @param funcionarios todos os funcionários (ativos e inativos)
     * @param inicio primeiro dia do período (inclusive), ou {@code null}
     * @param fim último dia do período (inclusive), ou {@code null}
     * @return resumo estimado do período
     */
    public static ResumoFinanceiro calcular(List<Locacao> locacoes,
            List<Peca> pecas, List<Funcionario> funcionarios,
            LocalDate inicio, LocalDate fim) {
        Map<Integer, Peca> pecasPorId = new HashMap<>();
        for (Peca peca : pecas) {
            pecasPorId.put(peca.idPeca(), peca);
        }
        Map<Integer, String> nomesPorId = new HashMap<>();
        for (Funcionario funcionario : funcionarios) {
            nomesPorId.put(funcionario.idFuncionario(), funcionario.nome());
        }

        Map<YearMonth, Acumulador> porMes = new TreeMap<>();
        Map<Integer, Acumulador> porFuncionario = new HashMap<>();
        Map<Integer, Acumulador> porPeca = new HashMap<>();
        BigDecimal receita = BigDecimal.ZERO;
        int totalLocacoes = 0;
        int totalPecas = 0;
        int semPreco = 0;

        for (Locacao locacao : locacoes) {
            if (!dentroDoPeriodo(locacao.dataEvento(), inicio, fim)) {
                continue;
            }
            BigDecimal valorLocacao = BigDecimal.ZERO;
            List<Integer> ids = locacao.idsPecas() == null
                    ? List.of() : locacao.idsPecas();
            for (Integer idPeca : ids) {
                if (idPeca == null) {
                    continue;
                }
                Peca peca = pecasPorId.get(idPeca);
                BigDecimal preco = peca == null ? null : peca.preco();
                if (preco == null) {
                    semPreco++;
                    preco = BigDecimal.ZERO;
                }
                valorLocacao = valorLocacao.add(preco);
                totalPecas++;
                porPeca.computeIfAbsent(idPeca, chave -> new Acumulador())
                        .somar(preco);
            }
            receita = receita.add(valorLocacao);
            totalLocacoes++;
            porMes.computeIfAbsent(YearMonth.from(locacao.dataEvento()),
                    chave -> new Acumulador()).somar(valorLocacao);
            porFuncionario.computeIfAbsent(locacao.idFuncionario(),
                    chave -> new Acumulador()).somar(valorLocacao);
        }

        BigDecimal ticketMedio = totalLocacoes == 0
                ? BigDecimal.ZERO.setScale(ESCALA_MONETARIA)
                : receita.divide(BigDecimal.valueOf(totalLocacoes),
                        ESCALA_MONETARIA, RoundingMode.HALF_UP);
        return new ResumoFinanceiro(
                dinheiro(receita), totalLocacoes, totalPecas, ticketMedio,
                semPreco, listarMeses(porMes),
                listarFuncionarios(porFuncionario, nomesPorId),
                listarPecas(porPeca, pecasPorId));
    }

    /**
     * Verifica se uma data está dentro do período.
     *
     * @param data data a verificar, possivelmente {@code null}
     * @param inicio primeiro dia (inclusive), ou {@code null}
     * @param fim último dia (inclusive), ou {@code null}
     * @return {@code true} se a data existe e está no período
     */
    private static boolean dentroDoPeriodo(LocalDate data, LocalDate inicio,
            LocalDate fim) {
        if (data == null) {
            return false;
        }
        boolean depoisDoInicio = inicio == null || !data.isBefore(inicio);
        boolean antesDoFim = fim == null || !data.isAfter(fim);
        return depoisDoInicio && antesDoFim;
    }

    /**
     * Converte os acumulados por mês em uma lista do mês mais antigo ao
     * mais novo.
     *
     * @param porMes acumulados ordenados por mês
     * @return lista de receitas mensais
     */
    private static List<ReceitaMensal> listarMeses(
            Map<YearMonth, Acumulador> porMes) {
        List<ReceitaMensal> meses = new ArrayList<>();
        for (Map.Entry<YearMonth, Acumulador> entrada : porMes.entrySet()) {
            Acumulador acumulado = entrada.getValue();
            meses.add(new ReceitaMensal(entrada.getKey(),
                    acumulado.quantidade, dinheiro(acumulado.valor)));
        }
        return meses;
    }

    /**
     * Converte os acumulados por funcionário em uma lista da maior para a
     * menor receita (empate: ordem alfabética).
     *
     * @param porFuncionario acumulados por id de funcionário
     * @param nomesPorId nomes dos funcionários conhecidos
     * @return lista de desempenhos
     */
    private static List<DesempenhoFuncionario> listarFuncionarios(
            Map<Integer, Acumulador> porFuncionario,
            Map<Integer, String> nomesPorId) {
        List<DesempenhoFuncionario> lista = new ArrayList<>();
        for (Map.Entry<Integer, Acumulador> entrada
                : porFuncionario.entrySet()) {
            Integer id = entrada.getKey();
            String nome;
            if (id == null) {
                nome = FUNCIONARIO_NAO_INFORMADO;
            } else {
                nome = nomesPorId.getOrDefault(id, "Funcionário #" + id);
            }
            Acumulador acumulado = entrada.getValue();
            lista.add(new DesempenhoFuncionario(nome, acumulado.quantidade,
                    dinheiro(acumulado.valor)));
        }
        lista.sort(Comparator
                .comparing(DesempenhoFuncionario::receita,
                        Comparator.reverseOrder())
                .thenComparing(DesempenhoFuncionario::nome));
        return lista;
    }

    /**
     * Converte os acumulados por peça em uma lista das mais alugadas
     * (empates: maior receita, depois código).
     *
     * @param porPeca acumulados por id de peça
     * @param pecasPorId peças do cadastro
     * @return até dez desempenhos, da peça mais locada para a menos
     */
    private static List<DesempenhoPeca> listarPecas(
            Map<Integer, Acumulador> porPeca, Map<Integer, Peca> pecasPorId) {
        List<DesempenhoPeca> lista = new ArrayList<>();
        for (Map.Entry<Integer, Acumulador> entrada : porPeca.entrySet()) {
            Peca peca = pecasPorId.get(entrada.getKey());
            String codigo = peca == null
                    ? CODIGO_PECA_REMOVIDA : peca.codigoUnico();
            String descricao = peca == null
                    ? "Peça removida (id " + entrada.getKey() + ")"
                    : peca.descricao();
            Acumulador acumulado = entrada.getValue();
            lista.add(new DesempenhoPeca(codigo, descricao,
                    acumulado.quantidade, dinheiro(acumulado.valor)));
        }
        lista.sort(Comparator
                .comparing(DesempenhoPeca::vezes, Comparator.reverseOrder())
                .thenComparing(DesempenhoPeca::receita,
                        Comparator.reverseOrder())
                .thenComparing(DesempenhoPeca::codigoUnico));
        if (lista.size() > LIMITE_PECAS_MAIS_ALUGADAS) {
            return new ArrayList<>(
                    lista.subList(0, LIMITE_PECAS_MAIS_ALUGADAS));
        }
        return lista;
    }

    /**
     * Padroniza um valor em reais com duas casas decimais.
     *
     * @param valor valor a padronizar
     * @return valor com duas casas decimais
     */
    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    /** Soma uma quantidade e um valor, usada nos agrupamentos. */
    private static final class Acumulador {

        /** Quantidade de itens somados. */
        private int quantidade;

        /** Valor somado, em reais. */
        private BigDecimal valor = BigDecimal.ZERO;

        /**
         * Soma um item ao acumulado.
         *
         * @param parcela valor do item, em reais
         */
        void somar(BigDecimal parcela) {
            quantidade++;
            valor = valor.add(parcela);
        }
    }
}
