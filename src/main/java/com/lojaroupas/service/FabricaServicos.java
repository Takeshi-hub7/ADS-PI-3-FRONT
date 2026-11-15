package com.lojaroupas.service;

import com.lojaroupas.service.api.AutenticacaoServiceApi;
import com.lojaroupas.service.api.ClienteServiceApi;
import com.lojaroupas.service.mock.AutenticacaoServiceMock;
import com.lojaroupas.service.mock.ClienteServiceMock;
import com.lojaroupas.util.Configuracao;

/**
 * Único ponto onde se escolhe a implementação de cada serviço.
 *
 * <p>No modo {@code mock} (padrão) usa dados falsos em memória. No modo
 * {@code api} usa as implementações que chamam o servidor configurado em
 * {@code api.url}. Os controllers não precisam mudar.</p>
 */
public final class FabricaServicos {

    /** Serviço de autenticação em uso; criado na primeira chamada. */
    private static AutenticacaoService autenticacaoService;

    /** Serviço de clientes em uso; criado na primeira chamada. */
    private static ClienteService clienteService;

    /** Impede a criação de instâncias da classe utilitária. */
    private FabricaServicos() {
    }

    /**
     * Retorna o serviço de autenticação, criando-o se necessário.
     *
     * @return serviço de autenticação em uso
     */
    public static synchronized AutenticacaoService
            obterAutenticacaoService() {
        if (autenticacaoService == null) {
            autenticacaoService = criarAutenticacaoService();
        }
        return autenticacaoService;
    }

    /**
     * Retorna o serviço de clientes, criando-o se necessário.
     *
     * <p>A instância é única para que os dados do mock persistam entre
     * as telas.</p>
     *
     * @return serviço de clientes em uso
     */
    public static synchronized ClienteService obterClienteService() {
        if (clienteService == null) {
            clienteService = criarClienteService();
        }
        return clienteService;
    }

    /**
     * Cria o serviço de autenticação conforme o modo configurado.
     *
     * @return nova instância do serviço
     */
    private static AutenticacaoService criarAutenticacaoService() {
        if (Configuracao.isModoMock()) {
            return new AutenticacaoServiceMock();
        }
        return new AutenticacaoServiceApi();
    }

    /**
     * Cria o serviço de clientes conforme o modo configurado.
     *
     * @return nova instância do serviço
     */
    private static ClienteService criarClienteService() {
        if (Configuracao.isModoMock()) {
            return new ClienteServiceMock();
        }
        return new ClienteServiceApi();
    }
}