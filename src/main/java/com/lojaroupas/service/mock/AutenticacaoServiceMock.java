package com.lojaroupas.service.mock;

import com.lojaroupas.model.PerfilAcesso;
import com.lojaroupas.model.Usuario;
import com.lojaroupas.service.AutenticacaoService;
import com.lojaroupas.service.ServiceException;

/**
 * Login FALSO, apenas para desenvolver a interface.
 * REMOVER na integração com a API.
 *
 * <p>Usuários de teste: {@code admin@loja.com} / {@code admin123}
 * (perfil ADMIN) e {@code func@loja.com} / {@code func123}
 * (perfil FUNCIONARIO).</p>
 */
public class AutenticacaoServiceMock implements AutenticacaoService {

    /** E-mail do administrador de teste. */
    private static final String EMAIL_ADMIN = "admin@loja.com";

    /** Senha do administrador de teste. */
    private static final String SENHA_ADMIN = "admin123";

    /** E-mail do funcionário de teste. */
    private static final String EMAIL_FUNCIONARIO = "func@loja.com";

    /** Senha do funcionário de teste. */
    private static final String SENHA_FUNCIONARIO = "func123";

    /** Mensagem exibida quando as credenciais não conferem. */
    private static final String MSG_CREDENCIAIS_INVALIDAS =
            "E-mail ou senha inválidos.";

    /**
     * Valida as credenciais contra os usuários de teste.
     *
     * @param email e-mail informado
     * @param senha senha informada
     * @return usuário de teste correspondente
     * @throws ServiceException se as credenciais não conferirem
     */
    @Override
    public Usuario autenticar(String email, String senha) {
        SimuladorRede.aguardar();

        if (EMAIL_ADMIN.equals(email) && SENHA_ADMIN.equals(senha)) {
            return new Usuario("Maria (Proprietária)", email,
                    PerfilAcesso.ADMIN, "token-falso-admin");
        }
        if (EMAIL_FUNCIONARIO.equals(email)
                && SENHA_FUNCIONARIO.equals(senha)) {
            return new Usuario("João (Funcionário)", email,
                    PerfilAcesso.FUNCIONARIO, "token-falso-funcionario");
        }
        throw new ServiceException(MSG_CREDENCIAIS_INVALIDAS);
    }
}