package com.lojaroupas.model;

/**
 * Perfis de acesso do sistema.
 *
 * <p>Os nomes das constantes devem coincidir com os valores aceitos pelo
 * campo {@code perfilAcesso} da API. Confirmar com o back-end.</p>
 */
public enum PerfilAcesso {

    /** Proprietário da loja: acesso a todas as telas. */
    ADMIN,

    /** Funcionário: sem acesso às telas de financeiro e funcionários. */
    FUNCIONARIO
}