package com.lojaroupas.service;

import com.lojaroupas.model.Locacao;
import java.util.List;

/**
 * Registro e consulta de locações.
 */
public interface LocacaoService {

    /**
     * Lista as locações registradas.
     *
     * @return locações
     */
    List<Locacao> listar();

    /**
     * Registra uma nova locação. A API recusa se alguma peça já estiver
     * reservada em período que se sobrepõe ao informado.
     *
     * @param locacao dados da locação, sem identificador
     * @return locação gravada, com identificador
     * @throws ServiceException se a API recusar (ex.: peça ocupada)
     */
    Locacao criar(Locacao locacao);

    /**
     * Cancela (exclui) uma locação.
     *
     * @param idLocacao identificador da locação
     */
    void excluir(Integer idLocacao);
}
