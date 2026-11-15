package com.lojaroupas.service.mock;

import com.lojaroupas.model.Funcionario;
import com.lojaroupas.service.FuncionarioService;
import java.util.List;

/**
 * Funcionários falsos em memória, para o modo de teste.
 */
public class FuncionarioServiceMock implements FuncionarioService {

    @Override
    public List<Funcionario> listarAtivos() {
        SimuladorRede.aguardar();
        return List.of(new Funcionario(1, "João (Funcionário)", null),
                new Funcionario(2, "Maria (Proprietária)", null));
    }
}
