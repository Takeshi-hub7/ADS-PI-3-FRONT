package com.lojaroupas.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Resumo financeiro ESTIMADO de um período.
 *
 * <p>Os valores são estimativas: cada locação vale a soma do preço atual
 * das peças que ela contém. O sistema ainda não guarda o preço cobrado na
 * hora da locação nem o sinal ou os pagamentos recebidos.</p>
 *
 * @param receitaEstimada soma do valor estimado de todas as locações
 * @param totalLocacoes quantidade de locações no período
 * @param totalPecasLocadas quantidade de peças em todas as locações
 * @param ticketMedio receita estimada dividida pelas locações (zero se
 *        não houver locações)
 * @param pecasSemPreco peças locadas sem preço no cadastro ou já
 *        removidas, contadas como R$ 0,00
 * @param porMes receita por mês do evento, do mais antigo ao mais novo
 * @param porFuncionario receita por funcionário, da maior para a menor
 * @param pecasMaisAlugadas até dez peças mais locadas, da mais locada
 *        para a menos locada
 */
public record ResumoFinanceiro(
        BigDecimal receitaEstimada,
        int totalLocacoes,
        int totalPecasLocadas,
        BigDecimal ticketMedio,
        int pecasSemPreco,
        List<ReceitaMensal> porMes,
        List<DesempenhoFuncionario> porFuncionario,
        List<DesempenhoPeca> pecasMaisAlugadas) {

    /**
     * Receita estimada de um mês.
     *
     * @param mes mês e ano da data do evento
     * @param locacoes quantidade de locações com evento no mês
     * @param receita receita estimada do mês
     */
    public record ReceitaMensal(YearMonth mes, int locacoes,
            BigDecimal receita) {
    }

    /**
     * Desempenho de um funcionário (o que registrou as locações).
     *
     * @param nome nome do funcionário
     * @param locacoes quantidade de locações registradas por ele
     * @param receita receita estimada dessas locações
     */
    public record DesempenhoFuncionario(String nome, int locacoes,
            BigDecimal receita) {
    }

    /**
     * Desempenho de uma peça.
     *
     * @param codigoUnico código único da peça ("—" se foi removida)
     * @param descricao descrição da peça
     * @param vezes quantidade de vezes que foi locada
     * @param receita receita estimada (vezes multiplicado pelo preço)
     */
    public record DesempenhoPeca(String codigoUnico, String descricao,
            int vezes, BigDecimal receita) {
    }
}
