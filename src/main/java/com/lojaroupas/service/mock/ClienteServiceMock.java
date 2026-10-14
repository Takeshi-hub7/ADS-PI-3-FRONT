package com.lojaroupas.service.mock;

import com.lojaroupas.model.Cliente;
import com.lojaroupas.service.ClienteService;
import com.lojaroupas.service.ServiceException;
import com.lojaroupas.util.Formatador;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clientes guardados em memória: os dados somem ao fechar o programa.
 *
 * <p>Imita regras que a API real também terá, como CPF único. Devolve
 * cópias dos objetos, como o JSON faria, para que a tela não altere a
 * base sem salvar.</p>
 */
public class ClienteServiceMock implements ClienteService {

    /** Mensagem exibida quando o cliente não existe mais na base. */
    private static final String MSG_NAO_ENCONTRADO =
            "Cliente não encontrado. Atualize a lista.";

    /** Mensagem exibida quando o CPF pertence a outro cliente. */
    private static final String MSG_CPF_DUPLICADO =
            "Já existe um cliente com este CPF.";

    /** Base de dados em memória. */
    private final List<Cliente> base = new ArrayList<>();

    /** Próximo identificador a ser atribuído. */
    private int proximoId = 1;

    /** Cria o serviço já com alguns clientes de exemplo. */
    public ClienteServiceMock() {
        semear("Ana Souza", "529.982.247-25", "12.345.678-9",
                LocalDate.of(1990, 5, 20));
        semear("Carlos Lima", "111.444.777-35", "",
                LocalDate.of(1985, 11, 3));
        semear("Beatriz Alves", "123.456.789-09", "98.765.432-1", null);
    }

    /**
     * Lista todos os clientes.
     *
     * @return cópias dos clientes existentes
     */
    @Override
    public synchronized List<Cliente> listar() {
        SimuladorRede.aguardar();
        return base.stream().map(this::copiar).toList();
    }

    /**
     * Cria ou atualiza um cliente, validando a unicidade do CPF.
     *
     * @param cliente cliente a salvar
     * @return cópia do cliente salvo
     * @throws ServiceException se o CPF já existir ou o cliente sumiu
     */
    @Override
    public synchronized Cliente salvar(Cliente cliente) {
        SimuladorRede.aguardar();
        verificarCpfDuplicado(cliente);

        if (cliente.getIdCliente() == null) {
            Cliente novo = copiar(cliente);
            novo.setIdCliente(proximoId++);
            base.add(novo);
            return copiar(novo);
        }

        for (int indice = 0; indice < base.size(); indice++) {
            Integer idExistente = base.get(indice).getIdCliente();
            if (idExistente.equals(cliente.getIdCliente())) {
                base.set(indice, copiar(cliente));
                return copiar(cliente);
            }
        }
        throw new ServiceException(MSG_NAO_ENCONTRADO);
    }

    /**
     * Exclui um cliente pelo identificador.
     *
     * @param idCliente identificador do cliente
     * @throws ServiceException se o cliente não existir
     */
    @Override
    public synchronized void excluir(Integer idCliente) {
        SimuladorRede.aguardar();
        boolean removido = base.removeIf(
                cliente -> cliente.getIdCliente().equals(idCliente));
        if (!removido) {
            throw new ServiceException(MSG_NAO_ENCONTRADO);
        }
    }

    /**
     * Adiciona um cliente de exemplo à base.
     *
     * @param nome nome do cliente
     * @param cpf CPF formatado
     * @param rg RG do cliente
     * @param nascimento data de nascimento, ou {@code null}
     */
    private void semear(String nome, String cpf, String rg,
            LocalDate nascimento) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(proximoId++);
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setRg(rg);
        cliente.setDataNascimento(nascimento);
        base.add(cliente);
    }

    /**
     * Garante que o CPF não pertence a outro cliente.
     *
     * @param cliente cliente que está sendo salvo
     * @throws ServiceException se o CPF já estiver em uso
     */
    private void verificarCpfDuplicado(Cliente cliente) {
        String cpfNovo = Formatador.somenteDigitos(cliente.getCpf());
        for (Cliente existente : base) {
            boolean outroCliente =
                    !existente.getIdCliente().equals(cliente.getIdCliente());
            String cpfExistente =
                    Formatador.somenteDigitos(existente.getCpf());
            if (outroCliente && cpfExistente.equals(cpfNovo)) {
                throw new ServiceException(MSG_CPF_DUPLICADO);
            }
        }
    }

    /**
     * Cria uma cópia independente de um cliente.
     *
     * @param origem cliente a copiar
     * @return nova instância com os mesmos dados
     */
    private Cliente copiar(Cliente origem) {
        Cliente copia = new Cliente();
        copia.setIdCliente(origem.getIdCliente());
        copia.setNome(origem.getNome());
        copia.setCpf(origem.getCpf());
        copia.setRg(origem.getRg());
        copia.setDataNascimento(origem.getDataNascimento());
        copia.setMedidas(origem.getMedidas());
        return copia;
    }
}