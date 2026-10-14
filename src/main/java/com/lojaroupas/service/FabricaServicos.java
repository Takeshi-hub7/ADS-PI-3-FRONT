package com.lojaroupas.service;

import com.lojaroupas.service.mock.AutenticacaoServiceMock;
import com.lojaroupas.service.mock.ClienteServiceMock;
import com.lojaroupas.util.Configuracao;

/**
 * Único ponto onde se escolhe a implementação de cada serviço.
 *
 * <p>No modo {@code mock} (padrão) usa dados falsos em memória. No modo
 * {@code api} deverá usar as implementações que chamam o servidor, que
 * ainda não existem: os pontos de integração estão marcados no código.
 * Os controllers não precisam mudar.</p>
 */
public final class FabricaServicos {

    /** Mensagem exibida quando o modo API é pedido antes de existir. */
    private static final String MSG_API_INDISPONIVEL =
            "A comunicação com a API ainda não foi implementada. "
            + "Use servico.modo=mock na configuração.";

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
     * @throws ServiceException se o modo configurado não estiver
     *         disponível
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
     * @throws ServiceException se o modo configurado não estiver
     *         disponível
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
     * @throws ServiceException se o modo API ainda não estiver pronto
     */
    private static AutenticacaoService criarAutenticacaoService() {
        if (Configuracao.isModoMock()) {
            return new AutenticacaoServiceMock();
        }
        // PONTO DE INTEGRAÇÃO: quando a API existir, retornar aqui
        // new AutenticacaoServiceApi(Configuracao.getUrlApi(), ...).
        throw new ServiceException(MSG_API_INDISPONIVEL);
    }

    /**
     * Cria o serviço de clientes conforme o modo configurado.
     *
     * @return nova instância do serviço
     * @throws ServiceException se o modo API ainda não estiver pronto
     */
    private static ClienteService criarClienteService() {
        if (Configuracao.isModoMock()) {
            return new ClienteServiceMock();
        }
        // PONTO DE INTEGRAÇÃO: quando a API existir, retornar aqui
        // new ClienteServiceApi(Configuracao.getUrlApi(), ...).
        throw new ServiceException(MSG_API_INDISPONIVEL);
    }
}