package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.Peca;
import com.lojaroupas.service.PecaService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
                pecas.add(new Peca(no.path("idPeca").asInt(),
                        no.path("codigoUnico").asText(""),
                        no.path("descricao").asText(""),
                        no.path("tamanho").asText(""),
                        no.path("cor").asText(""),
                        no.path("preco").isMissingNode()
                                ? BigDecimal.ZERO
                                : no.path("preco").decimalValue()));
            }
        }
        return pecas;
    }
}
