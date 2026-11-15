package com.lojaroupas.service;

import com.lojaroupas.model.Peca;
import java.util.List;

/**
 * Consulta das peças do acervo.
 */
public interface PecaService {

    /**
     * Lista todas as peças cadastradas.
     *
     * @return peças do acervo
     */
    List<Peca> listar();
}
