package com.lojaroupas.util;

import com.lojaroupas.model.PerfilAcesso;
import com.lojaroupas.model.Usuario;

/**
 * Guarda em memória quem está logado e o endereço do servidor.
 *
 * <p>Nada é gravado em disco. A senha nunca deve ser armazenada.</p>
 */
public final class Sessao {

    /** Usuário autenticado; {@code null} se não há sessão ativa. */
    private static Usuario usuarioLogado;

    /** Endereço base da API informado no login. */
    private static String urlServidor;

    /** Impede a criação de instâncias da classe utilitária. */
    private Sessao() {
    }

    /**
     * Inicia uma sessão após o login.
     *
     * @param usuario usuário autenticado
     * @param url endereço base da API
     */
    public static void iniciar(Usuario usuario, String url) {
        usuarioLogado = usuario;
        urlServidor = url;
    }

    /** Encerra a sessão, descartando usuário e endereço do servidor. */
    public static void encerrar() {
        usuarioLogado = null;
        urlServidor = null;
    }

    /**
     * Retorna o usuário autenticado.
     *
     * @return usuário logado, ou {@code null} se não há sessão
     */
    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    /**
     * Retorna o endereço base da API.
     *
     * @return endereço do servidor, ou {@code null} se não há sessão
     */
    public static String getUrlServidor() {
        return urlServidor;
    }

    /**
     * Informa se o usuário logado é administrador (proprietário).
     *
     * @return {@code true} se há sessão e o perfil é ADMIN
     */
    public static boolean isAdministrador() {
        return usuarioLogado != null
                && usuarioLogado.getPerfilAcesso() == PerfilAcesso.ADMIN;
    }
}