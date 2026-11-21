package com.lojaroupas.service;

import com.lojaroupas.model.Funcionario;
import java.util.List;

/**
 * Cadastro e consulta dos funcionários da loja (RF-35).
 */
public interface FuncionarioService {

    /**
     * Lista os funcionários ativos, que podem atender uma locação.
     *
     * @return funcionários ativos
     */
    List<Funcionario> listarAtivos();

    /**
     * Lista todos os funcionários, ativos e inativos.
     *
     * @return funcionários
     */
    List<Funcionario> listarTodos();

    /**
     * Cadastra um funcionário novo, criando antes o login dele.
     *
     * @param funcionario dados do funcionário, sem identificador
     * @param email e-mail de login
     * @param senha senha inicial
     * @return funcionário gravado
     */
    Funcionario cadastrar(Funcionario funcionario, String email, String senha);

    /**
     * Atualiza os dados de um funcionário existente.
     *
     * @param funcionario dados com o identificador
     * @return funcionário gravado
     */
    Funcionario atualizar(Funcionario funcionario);

    /**
     * Ativa ou desativa um funcionário, sem apagar o cadastro.
     *
     * @param idFuncionario identificador
     * @param ativo {@code true} para ativar
     * @return funcionário com a nova situação
     */
    Funcionario definirAtivo(Integer idFuncionario, boolean ativo);
}
