package com.lojaroupas.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.lojaroupas.model.PerfilAcesso;
import com.lojaroupas.model.Usuario;
import com.lojaroupas.service.AutenticacaoService;
import com.lojaroupas.service.ServiceException;
import java.util.Map;

/**
 * Autenticação pela API ({@code POST /usuarios/login}).
 */
public class AutenticacaoServiceApi implements AutenticacaoService {

    /** Rota de login da API. */
    private static final String ROTA_LOGIN = "/usuarios/login";

    /** Perfil do administrador, como gravado no banco. */
    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";

    /** Perfil do funcionário, como gravado no banco. */
    private static final String PERFIL_FUNCIONARIO = "FUNCIONARIO";

    /** Mensagem para perfis que não usam o sistema da loja. */
    private static final String MSG_SEM_ACESSO =
            "Este usuário não tem acesso ao sistema da loja.";

    /** Conexão com a API. */
    private final ConexaoApi conexao = new ConexaoApi();

    @Override
    public Usuario autenticar(String email, String senha) {
        JsonNode resposta = conexao.enviar("POST", ROTA_LOGIN,
                Map.of("email", email, "senha", senha));
        String perfil = resposta.path("perfilAcesso").asText("");
        PerfilAcesso perfilAcesso;
        if (PERFIL_ADMINISTRADOR.equalsIgnoreCase(perfil)) {
            perfilAcesso = PerfilAcesso.ADMIN;
        } else if (PERFIL_FUNCIONARIO.equalsIgnoreCase(perfil)) {
            perfilAcesso = PerfilAcesso.FUNCIONARIO;
        } else {
            throw new ServiceException(MSG_SEM_ACESSO);
        }
        // A API ainda não emite token; o campo fica vazio até existir.
        return new Usuario(resposta.path("idUsuario").asInt(),
                resposta.path("nome").asText(),
                resposta.path("email").asText(), perfilAcesso, "");
    }
}
