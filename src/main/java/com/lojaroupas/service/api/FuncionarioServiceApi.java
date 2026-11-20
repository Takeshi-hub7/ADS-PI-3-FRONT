package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.Funcionario;
import com.lojaroupas.service.FuncionarioService;
import com.lojaroupas.service.ServiceException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Funcionários pela API ({@code /funcionarios} e {@code /usuarios}).
 */
public class FuncionarioServiceApi implements FuncionarioService {

    /** Rota de funcionários na API. */
    private static final String ROTA = "/funcionarios";

    /** Rota de usuários (logins) na API. */
    private static final String ROTA_USUARIOS = "/usuarios";

    /** Perfil de acesso dado ao login de um funcionário novo. */
    private static final String PERFIL_FUNCIONARIO = "FUNCIONARIO";

    /** Conexão com a API. */
    private final ConexaoApi conexao = new ConexaoApi();

    @Override
    public List<Funcionario> listarAtivos() {
        return listarTodos().stream().filter(Funcionario::ativo).toList();
    }

    @Override
    public List<Funcionario> listarTodos() {
        JsonNode resposta = conexao.enviar("GET", ROTA, null);
        List<Funcionario> funcionarios = new ArrayList<>();
        if (resposta != null) {
            for (JsonNode no : resposta) {
                funcionarios.add(converter(no));
            }
        }
        return funcionarios;
    }

    @Override
    public Funcionario cadastrar(Funcionario f, String email, String senha) {
        Map<String, Object> usuario = new LinkedHashMap<>();
        usuario.put("nome", f.nome());
        usuario.put("email", email);
        usuario.put("senha", senha);
        usuario.put("perfilAcesso", PERFIL_FUNCIONARIO);
        int idUsuario = conexao.enviar("POST", ROTA_USUARIOS, usuario)
                .path("idUsuario").asInt();
        try {
            return converter(conexao.enviar("POST", ROTA,
                    montarCorpo(f, idUsuario)));
        } catch (ServiceException e) {
            // Desfaz o login criado, para não sobrar usuário sem funcionário.
            try {
                conexao.enviar("DELETE", ROTA_USUARIOS + "/" + idUsuario, null);
            } catch (ServiceException ignorada) {
                // mantém a mensagem original do erro do funcionário
            }
            throw e;
        }
    }

    @Override
    public Funcionario atualizar(Funcionario f) {
        return converter(conexao.enviar("PUT", ROTA + "/" + f.idFuncionario(),
                montarCorpo(f, f.idUsuario())));
    }

    @Override
    public Funcionario definirAtivo(Integer idFuncionario, boolean ativo) {
        return converter(conexao.enviar("PATCH", ROTA + "/" + idFuncionario
                + (ativo ? "/ativar" : "/desativar"), null));
    }

    /**
     * Monta o corpo no formato de {@code FuncionarioRequest}.
     *
     * @param f funcionário
     * @param idUsuario login vinculado
     * @return mapa convertido em JSON pela conexão
     */
    private Map<String, Object> montarCorpo(Funcionario f, Integer idUsuario) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("idUsuario", idUsuario);
        corpo.put("nome", f.nome());
        corpo.put("cpf", f.cpf());
        corpo.put("rg", vazio(f.rg()));
        corpo.put("telefone", vazio(f.telefone()));
        corpo.put("ctps", vazio(f.ctps()));
        corpo.put("dependente", f.dependente());
        corpo.put("sexo", vazio(f.sexo()));
        corpo.put("salario", f.salario());
        corpo.put("dataNascimento", f.dataNascimento());
        corpo.put("estadoCivil", vazio(f.estadoCivil()));
        return corpo;
    }

    /**
     * Troca texto em branco por {@code null}.
     *
     * @param texto texto
     * @return texto ou {@code null}
     */
    private static String vazio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /**
     * Converte um funcionário da resposta da API.
     *
     * @param no objeto JSON de {@code FuncionarioResponse}
     * @return funcionário da tela
     */
    private Funcionario converter(JsonNode no) {
        return new Funcionario(
                no.path("idFuncionario").asInt(),
                no.path("nome").asText(""),
                no.path("idUsuario").isNull() ? null : no.path("idUsuario").asInt(),
                texto(no, "cpf"), texto(no, "rg"), texto(no, "telefone"),
                texto(no, "ctps"),
                no.path("dependente").asInt(0),
                texto(no, "sexo"),
                no.path("salario").isNull() || no.path("salario").isMissingNode()
                        ? null : no.path("salario").decimalValue()
                                .setScale(2, java.math.RoundingMode.HALF_UP),
                no.path("dataNascimento").isNull()
                        || no.path("dataNascimento").isMissingNode()
                        ? null : LocalDate.parse(no.path("dataNascimento").asText()),
                texto(no, "estadoCivil"),
                no.path("ativo").asBoolean(true));
    }

    /**
     * Lê um campo de texto que pode vir nulo.
     *
     * @param no objeto JSON
     * @param campo nome do campo
     * @return texto, ou vazio se nulo
     */
    private static String texto(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        return valor.isNull() || valor.isMissingNode() ? "" : valor.asText();
    }
}
