package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.Locacao;
import com.lojaroupas.service.LocacaoService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Locações pela API ({@code /locacoes}).
 */
public class LocacaoServiceApi implements LocacaoService {

    /** Rota de locações na API. */
    private static final String ROTA = "/locacoes";

    /** Conexão com a API. */
    private final ConexaoApi conexao = new ConexaoApi();

    @Override
    public List<Locacao> listar() {
        JsonNode resposta = conexao.enviar("GET", ROTA, null);
        List<Locacao> locacoes = new ArrayList<>();
        if (resposta != null) {
            for (JsonNode no : resposta) {
                locacoes.add(converter(no));
            }
        }
        return locacoes;
    }

    @Override
    public Locacao criar(Locacao locacao) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("idCliente", locacao.idCliente());
        corpo.put("idUsuario", locacao.idUsuario());
        corpo.put("idFuncionario", locacao.idFuncionario());
        corpo.put("dataEvento", locacao.dataEvento());
        corpo.put("dataDevolucao", locacao.dataDevolucao());
        corpo.put("idsPecas", locacao.idsPecas());
        return converter(conexao.enviar("POST", ROTA, corpo));
    }

    @Override
    public void excluir(Integer idLocacao) {
        conexao.enviar("DELETE", ROTA + "/" + idLocacao, null);
    }

    /**
     * Converte uma locação da resposta da API.
     *
     * @param no objeto JSON de {@code LocacaoResponse}
     * @return locação
     */
    private Locacao converter(JsonNode no) {
        List<Integer> pecas = new ArrayList<>();
        for (JsonNode id : no.path("idsPecas")) {
            pecas.add(id.asInt());
        }
        return new Locacao(no.path("idLocacao").asInt(),
                no.path("idCliente").asInt(),
                no.path("idUsuario").asInt(),
                no.path("idFuncionario").asInt(),
                data(no, "dataEvento"), data(no, "dataDevolucao"), pecas);
    }

    /**
     * Lê uma data no formato aaaa-mm-dd, tratando ausência como nula.
     *
     * @param no objeto JSON
     * @param campo nome do campo
     * @return data, ou {@code null}
     */
    private LocalDate data(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        return valor == null || valor.isNull()
                ? null : LocalDate.parse(valor.asText());
    }
}
