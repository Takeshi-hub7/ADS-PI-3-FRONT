package com.lojaroupas.service;

import com.lojaroupas.model.Usuario;

/**
 * Contrato de autenticação.
 *
 * <p>A versão real chamará o endpoint de login da API.</p>
 */
public interface AutenticacaoService {

    /**
     * Valida as credenciais informadas.
     *
     * @param email e-mail do usuário
     * @param senha senha do usuário
     * @return usuário autenticado
     * @throws ServiceException se o e-mail ou a senha forem inválidos
     */
    Usuario autenticar(String email, String senha);
}