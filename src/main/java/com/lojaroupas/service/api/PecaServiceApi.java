package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.Peca;
import com.lojaroupas.service.PecaService;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Peças pela API ({@code /produtos}).
 */
public class PecaServiceApi implements PecaService {

    /** Rota de produtos (peças) na API. */
    private static final String ROTA = "/produtos";

    /** Conexão com a API. */
    private final ConexaoApi conexao = new ConexaoApi();

    @Override
    public List<Peca> listar() {
        JsonNode resposta = conexao.enviar("GET", ROTA, null);
        List<Peca> pecas = new ArrayList<>();
        if (resposta != null) {
            for (JsonNode no : resposta) {
                pecas.add(converter(no));
            }
        }
        return pecas;
    }

    @Override
    public Peca buscarPorCodigo(String codigo) {
        String caminho = ROTA + "/codigo/"
                + URLEncoder.encode(codigo.trim(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
        return converter(conexao.enviar("GET", caminho, null));
    }

    @Override
    public Peca salvar(Peca peca) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigoUnico", peca.codigoUnico());
        corpo.put("descricao", peca.descricao());
        corpo.put("tamanho", peca.tamanho());
        corpo.put("cor", peca.cor());
        corpo.put("material", peca.material() == null
                || peca.material().isBlank() ? null : peca.material());
        corpo.put("preco", peca.preco());
        JsonNode resposta = peca.idPeca() == null
                ? conexao.enviar("POST", ROTA, corpo)
                : conexao.enviar("PUT", ROTA + "/" + peca.idPeca(), corpo);
        return converter(resposta);
    }

    @Override
    public void excluir(Integer idPeca) {
        conexao.enviar("DELETE", ROTA + "/" + idPeca, null);
    }

    /**
     * Converte uma peça da resposta da API.
     *
     * @param no objeto JSON de {@code ProdutoResponse}
     * @return peça da tela
     */
    private Peca converter(JsonNode no) {
        return new Peca(no.path("idPeca").asInt(),
                no.path("codigoUnico").asText(""),
                no.path("descricao").asText(""),
                no.path("tamanho").asText(""),
                no.path("cor").asText(""),
                no.path("material").isNull() ? ""
                        : no.path("material").asText(""),
                no.path("preco").isMissingNode()
                        ? BigDecimal.ZERO
                        : no.path("preco").decimalValue());
    }
}
