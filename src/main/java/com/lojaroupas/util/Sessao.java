package com.lojaroupas.util;

import com.lojaroupas.model.PerfilAcesso;
import com.lojaroupas.model.Usuario;

/**
 * Guarda em memória quem está logado.
 *
 * <p>Nada é gravado em disco. A senha nunca deve ser armazenada. O
 * endereço do servidor não fica aqui: vem da classe
 * {@link Configuracao}.</p>
 */
public final class Sessao {

    /** Usuário autenticado; {@code null} se não há sessão ativa. */
    private static Usuario usuarioLogado;

    /** Impede a criação de instâncias da classe utilitária. */
    private Sessao() {
    }

    /**
     * Inicia uma sessão após o login.
     *
     * @param usuario usuário autenticado
     */
    public static void iniciar(Usuario usuario) {
        usuarioLogado = usuario;
    }

    /** Encerra a sessão, descartando o usuário logado. */
    public static void encerrar() {
        usuarioLogado = null;
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
     * Informa se o usuário logado é administrador (proprietário).
     *
     * @return {@code true} se há sessão e o perfil é ADMIN
     */
    public static boolean isAdministrador() {
        return usuarioLogado != null
                && usuarioLogado.getPerfilAcesso() == PerfilAcesso.ADMIN;
    }
}