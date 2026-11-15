package com.lojaroupas.service;

import com.lojaroupas.model.Funcionario;
import java.util.List;

/**
 * Consulta dos funcionários da loja.
 */
public interface FuncionarioService {

    /**
     * Lista os funcionários ativos, que podem atender uma locação.
     *
     * @return funcionários ativos
     */
    List<Funcionario> listarAtivos();
}
