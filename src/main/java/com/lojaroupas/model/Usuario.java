package com.lojaroupas.model;

/**
 * Usuário autenticado no sistema.
 *
 * <p>Objeto imutável criado após o login bem-sucedido. O token será
 * enviado à API em cada requisição para identificar o usuário.</p>
 */
public final class Usuario {

    /** Nome de exibição do usuário. */
    private final String nome;

    /** E-mail utilizado no login. */
    private final String email;

    /** Perfil de acesso do usuário. */
    private final PerfilAcesso perfilAcesso;

    /** Token de autenticação fornecido pela API. */
    private final String token;

    /**
     * Cria um usuário autenticado.
     *
     * @param nome nome de exibição
     * @param email e-mail utilizado no login
     * @param perfilAcesso perfil de acesso
     * @param token token de autenticação
     */
    public Usuario(String nome, String email, PerfilAcesso perfilAcesso,
            String token) {
        this.nome = nome;
        this.email = email;
        this.perfilAcesso = perfilAcesso;
        this.token = token;
    }

    /**
     * Retorna o nome de exibição.
     *
     * @return nome do usuário
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o e-mail utilizado no login.
     *
     * @return e-mail do usuário
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retorna o perfil de acesso.
     *
     * @return perfil de acesso do usuário
     */
    public PerfilAcesso getPerfilAcesso() {
        return perfilAcesso;
    }

    /**
     * Retorna o token de autenticação.
     *
     * @return token enviado à API nas requisições
     */
    public String getToken() {
        return token;
    }
}