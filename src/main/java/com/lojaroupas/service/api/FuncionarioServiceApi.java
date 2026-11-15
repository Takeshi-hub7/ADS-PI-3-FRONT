package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.Funcionario;
import com.lojaroupas.service.FuncionarioService;
import java.util.ArrayList;
import java.util.List;

/**
 * Funcionários pela API ({@code /funcionarios}).
 */
public class FuncionarioServiceApi implements FuncionarioService {

    /** Rota de funcionários na API. */
    private static final String ROTA = "/funcionarios";

    /** Conexão com a API. */
    private final ConexaoApi conexao = new ConexaoApi();

    @Override
    public List<Funcionario> listarAtivos() {
        JsonNode resposta = conexao.enviar("GET", ROTA, null);
        List<Funcionario> funcionarios = new ArrayList<>();
        if (resposta != null) {
            for (JsonNode no : resposta) {
                if (!no.path("ativo").asBoolean(true)) {
                    continue;
                }
                JsonNode usuario = no.get("idUsuario");
                funcionarios.add(new Funcionario(
                        no.path("idFuncionario").asInt(),
                        no.path("nome").asText(""),
                        usuario == null || usuario.isNull()
                                ? null : usuario.asInt()));
            }
        }
        return funcionarios;
    }
}
