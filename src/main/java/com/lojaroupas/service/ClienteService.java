package com.lojaroupas.service;

import com.lojaroupas.model.Cliente;
import java.util.List;

/**
 * Contrato de acesso aos clientes.
 *
 * <p>Os controllers conhecem somente esta interface. Hoje a implementação
 * é um mock em memória; depois haverá uma que chama a API.</p>
 */
public interface ClienteService {

    /**
     * Lista todos os clientes.
     *
     * @return lista de clientes
     * @throws ServiceException se não for possível obter a lista
     */
    List<Cliente> listar();

    /**
     * Cria um cliente novo ou atualiza um existente.
     *
     * @param cliente cliente a salvar; sem id cria, com id atualiza
     * @return cliente salvo, já com o identificador
     * @throws ServiceException se a operação for rejeitada
     */
    Cliente salvar(Cliente cliente);

    /**
     * Exclui um cliente.
     *
     * @param idCliente identificador do cliente
     * @throws ServiceException se o cliente não existir
     */
    void excluir(Integer idCliente);
}