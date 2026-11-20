package com.lojaroupas.service;

import com.lojaroupas.model.Peca;
import java.util.List;

/**
 * Cadastro e consulta das peças do acervo.
 */
public interface PecaService {

    /**
     * Lista todas as peças cadastradas.
     *
     * @return peças do acervo
     */
    List<Peca> listar();

    /**
     * Busca uma peça pelo código da etiqueta (RF-19).
     *
     * @param codigo código único, como TRJ-001
     * @return a peça encontrada
     * @throws ServiceException se não houver peça com esse código
     */
    Peca buscarPorCodigo(String codigo);

    /**
     * Cadastra uma peça nova ou atualiza uma existente.
     *
     * @param peca peça da tela; sem id, é cadastro novo
     * @return a peça como ficou gravada
     */
    Peca salvar(Peca peca);

    /**
     * Exclui uma peça.
     *
     * @param idPeca identificador da peça
     */
    void excluir(Integer idPeca);
}
