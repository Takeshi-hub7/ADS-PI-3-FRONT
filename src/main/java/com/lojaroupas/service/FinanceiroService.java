package com.lojaroupas.service;

import com.lojaroupas.model.ResumoFinanceiro;
import java.time.LocalDate;

/**
 * Contrato do resumo financeiro.
 *
 * <p>Hoje a implementação é uma ESTIMATIVA calculada a partir das
 * locações, das peças e dos funcionários. Quando o back-end tiver o módulo
 * de pagamento, basta trocar a implementação aqui, sem mexer na tela.</p>
 */
public interface FinanceiroService {

    /**
     * Calcula o resumo financeiro de um período.
     *
     * @param inicio primeiro dia do período (inclusive), ou {@code null}
     *        para não limitar o início
     * @param fim último dia do período (inclusive), ou {@code null} para
     *        não limitar o fim
     * @return resumo do período, atribuído pela data do evento de cada
     *         locação
     * @throws ServiceException se não for possível obter os dados
     */
    ResumoFinanceiro obterResumo(LocalDate inicio, LocalDate fim);
}
