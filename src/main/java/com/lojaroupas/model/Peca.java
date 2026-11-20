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
 * @param material tecido principal (opcional)
 * @param preco valor do aluguel
 */
public record Peca(Integer idPeca, String codigoUnico, String descricao,
        String tamanho, String cor, String material, BigDecimal preco) {

    /**
     * Cria uma peça sem material informado.
     *
     * @param idPeca identificador
     * @param codigoUnico código da etiqueta
     * @param descricao descrição
     * @param tamanho tamanho
     * @param cor cor
     * @param preco valor do aluguel
     */
    public Peca(Integer idPeca, String codigoUnico, String descricao,
            String tamanho, String cor, BigDecimal preco) {
        this(idPeca, codigoUnico, descricao, tamanho, cor, null, preco);
    }

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
