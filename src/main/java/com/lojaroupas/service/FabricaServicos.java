package com.lojaroupas.service;

import com.lojaroupas.service.mock.AutenticacaoServiceMock;
import com.lojaroupas.service.mock.ClienteServiceMock;

/**
 * Único ponto onde se escolhe a implementação de cada serviço.
 *
 * <p>Na integração com a API, troque {@code new ...Mock()} pela
 * implementação real. Os controllers não precisam mudar.</p>
 */
public final class FabricaServicos {

    /** Serviço de autenticação em uso. */
    private static final AutenticacaoService AUTENTICACAO_SERVICE =
            new AutenticacaoServiceMock();

    /** Serviço de clientes em uso (instância única, mantém os dados). */
    private static final ClienteService CLIENTE_SERVICE =
            new ClienteServiceMock();

    /** Impede a criação de instâncias da classe utilitária. */
    private FabricaServicos() {
    }

    /**
     * Retorna o serviço de autenticação.
     *
     * @return serviço de autenticação em uso
     */
    public static AutenticacaoService obterAutenticacaoService() {
        return AUTENTICACAO_SERVICE;
    }

    /**
     * Retorna o serviço de clientes.
     *
     * @return serviço de clientes em uso
     */
    public static ClienteService obterClienteService() {
        return CLIENTE_SERVICE;
    }
}