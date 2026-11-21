package com.lojaroupas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Funcionário da loja, como a API devolve em {@code FuncionarioResponse}.
 *
 * @param idFuncionario identificador gerado pelo banco
 * @param nome nome do funcionário
 * @param idUsuario usuário de acesso (login) vinculado
 * @param cpf CPF no formato 000.000.000-00
 * @param rg RG
 * @param telefone telefone
 * @param ctps número da carteira de trabalho
 * @param dependente quantidade de dependentes
 * @param sexo sexo
 * @param salario salário mensal
 * @param dataNascimento data de nascimento
 * @param estadoCivil estado civil
 * @param ativo se o funcionário está ativo
 */
public record Funcionario(Integer idFuncionario, String nome,
        Integer idUsuario, String cpf, String rg, String telefone,
        String ctps, Integer dependente, String sexo, BigDecimal salario,
        LocalDate dataNascimento, String estadoCivil, boolean ativo) {

    /**
     * Cria um funcionário só com os dados usados na escolha do atendente.
     *
     * @param idFuncionario identificador
     * @param nome nome
     * @param idUsuario usuário vinculado
     */
    public Funcionario(Integer idFuncionario, String nome, Integer idUsuario) {
        this(idFuncionario, nome, idUsuario, null, null, null, null, 0,
                null, null, null, null, true);
    }

    /**
     * Texto usado na caixa de seleção.
     *
     * @return nome do funcionário
     */
    @Override
    public String toString() {
        return nome;
    }
}
