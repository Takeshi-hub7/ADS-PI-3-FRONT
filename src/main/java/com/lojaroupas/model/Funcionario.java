package com.lojaroupas.model;

/**
 * Funcionário da loja, usado para indicar o atendente da locação.
 *
 * @param idFuncionario identificador gerado pelo banco
 * @param nome nome do funcionário
 * @param idUsuario usuário de acesso vinculado, se houver
 */
public record Funcionario(Integer idFuncionario, String nome,
        Integer idUsuario) {

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
