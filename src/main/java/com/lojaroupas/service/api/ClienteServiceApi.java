package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.Cliente;
import com.lojaroupas.service.ClienteService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Clientes pela API ({@code /cliente}).
 */
public class ClienteServiceApi implements ClienteService {

    /** Rota base de clientes na API. */
    private static final String ROTA = "/cliente";

    /** Conexão com a API. */
    private final ConexaoApi conexao = new ConexaoApi();

    @Override
    public List<Cliente> listar() {
        JsonNode resposta = conexao.enviar("GET", ROTA, null);
        List<Cliente> clientes = new ArrayList<>();
        if (resposta != null) {
            for (JsonNode no : resposta) {
                clientes.add(converter(no));
            }
        }
        return clientes;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        Map<String, Object> corpo = montarCorpo(cliente);
        JsonNode resposta = cliente.getIdCliente() == null
                ? conexao.enviar("POST", ROTA, corpo)
                : conexao.enviar("PUT", ROTA + "/" + cliente.getIdCliente(),
                        corpo);
        return converter(resposta);
    }

    @Override
    public void excluir(Integer idCliente) {
        conexao.enviar("DELETE", ROTA + "/" + idCliente, null);
    }

    /**
     * Monta o corpo no formato de {@code ClienteRequest} da API.
     *
     * <p>Campos opcionais vazios vão como {@code null}, para o banco não
     * guardar texto em branco.</p>
     *
     * @param cliente cliente da tela
     * @return mapa convertido em JSON pela conexão
     */
    private Map<String, Object> montarCorpo(Cliente cliente) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", cliente.getNome());
        corpo.put("cpf", cliente.getCpf());
        corpo.put("telefone", cliente.getTelefone());
        corpo.put("endereco", cliente.getEndereco());
        corpo.put("rg", vazioComoNulo(cliente.getRg()));
        corpo.put("medidas", vazioComoNulo(cliente.getMedidas()));
        corpo.put("dataNascimento", cliente.getDataNascimento());
        corpo.put("idUsuario", cliente.getIdUsuario());
        return corpo;
    }

    /**
     * Converte um cliente da resposta da API.
     *
     * @param no objeto JSON de {@code ClienteResponse}
     * @return cliente da tela
     */
    private Cliente converter(JsonNode no) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(no.path("idCliente").asInt());
        cliente.setNome(texto(no, "nome"));
        cliente.setCpf(texto(no, "cpf"));
        cliente.setTelefone(texto(no, "telefone"));
        cliente.setEndereco(texto(no, "endereco"));
        cliente.setRg(texto(no, "rg"));
        cliente.setMedidas(texto(no, "medidas"));
        JsonNode usuario = no.get("idUsuario");
        cliente.setIdUsuario(usuario == null || usuario.isNull()
                ? null : usuario.asInt());
        String nascimento = texto(no, "dataNascimento");
        cliente.setDataNascimento(nascimento.isEmpty()
                ? null : LocalDate.parse(nascimento));
        return cliente;
    }

    /**
     * Lê um campo de texto, tratando ausência e {@code null} como vazio.
     *
     * @param no objeto JSON
     * @param campo nome do campo
     * @return valor do campo, ou texto vazio
     */
    private String texto(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        return valor == null || valor.isNull() ? "" : valor.asText();
    }

    /**
     * Troca texto vazio por {@code null}.
     *
     * @param valor texto do formulário
     * @return o próprio texto, ou {@code null} se vazio
     */
    private String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }
}
