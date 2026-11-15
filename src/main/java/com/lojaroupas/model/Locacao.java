package com.lojaroupas.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Locação (reserva) de uma ou mais peças para um evento.
 *
 * @param idLocacao identificador gerado pelo banco; {@code null} se nova
 * @param idCliente cliente que aluga
 * @param idUsuario usuário que registrou a locação
 * @param idFuncionario funcionário responsável pelo atendimento
 * @param dataEvento data do evento
 * @param dataDevolucao data prevista de devolução
 * @param idsPecas peças reservadas
 */
public record Locacao(Integer idLocacao, Integer idCliente,
        Integer idUsuario, Integer idFuncionario, LocalDate dataEvento,
        LocalDate dataDevolucao, List<Integer> idsPecas) {
}
