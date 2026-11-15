package com.lojaroupas.model;

import java.math.BigDecimal;

/**
 * Peça do acervo, como a API devolve em {@code ProdutoResponse}.
 *
 * @param idPeca identificador gerado pelo banco
 * @param codigoUnico código da etiqueta (ex.: TRJ-001)
 * @param descricao descrição da peça
 * @param tamanho tamanho
 * @param cor cor
 * @param preco valor do aluguel
 */
public record Peca(Integer idPeca, String codigoUnico, String descricao,
        String tamanho, String cor, BigDecimal preco) {

    /**
     * Texto usado nas listas: código, descrição e tamanho.
     *
     * @return descrição curta da peça
     */
    @Override
    public String toString() {
        return codigoUnico + " · " + descricao + " (" + tamanho + ")";
    }
}
