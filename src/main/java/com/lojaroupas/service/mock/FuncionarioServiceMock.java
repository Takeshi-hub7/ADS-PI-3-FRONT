package com.lojaroupas.service.mock;

import com.lojaroupas.model.Funcionario;
import com.lojaroupas.service.FuncionarioService;
import com.lojaroupas.service.ServiceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Funcionários falsos em memória, para o modo de teste.
 */
public class FuncionarioServiceMock implements FuncionarioService {

    /** Funcionários de exemplo, que podem ser alterados durante o uso. */
    private static final List<Funcionario> LISTA = new ArrayList<>(List.of(
            new Funcionario(1, "João Pereira", 2, "444.555.666-77", "",
                    "(19) 95432-1098", "1234567/0001", 1, "Masculino",
                    new BigDecimal("2800.00"), LocalDate.of(1992, 3, 25),
                    "Casado", true),
            new Funcionario(2, "Maria Souza", 3, "555.666.777-88", "",
                    "(19) 94321-0987", "2345678/0001", 0, "Feminino",
                    new BigDecimal("2500.00"), LocalDate.of(1997, 7, 11),
                    "Solteira", true)));

    /** Próximo identificador livre. */
    private static int proximoId = 3;

    @Override
    public List<Funcionario> listarAtivos() {
        return listarTodos().stream().filter(Funcionario::ativo).toList();
    }

    @Override
    public List<Funcionario> listarTodos() {
        SimuladorRede.aguardar();
        synchronized (LISTA) {
            return List.copyOf(LISTA);
        }
    }

    @Override
    public Funcionario cadastrar(Funcionario f, String email, String senha) {
        SimuladorRede.aguardar();
        synchronized (LISTA) {
            verificarCpf(f);
            Funcionario novo = new Funcionario(proximoId++, f.nome(), null,
                    f.cpf(), f.rg(), f.telefone(), f.ctps(), f.dependente(),
                    f.sexo(), f.salario(), f.dataNascimento(),
                    f.estadoCivil(), true);
            LISTA.add(novo);
            return novo;
        }
    }

    @Override
    public Funcionario atualizar(Funcionario f) {
        SimuladorRede.aguardar();
        synchronized (LISTA) {
            verificarCpf(f);
            LISTA.replaceAll(x -> x.idFuncionario().equals(f.idFuncionario())
                    ? f : x);
            return f;
        }
    }

    @Override
    public Funcionario definirAtivo(Integer idFuncionario, boolean ativo) {
        SimuladorRede.aguardar();
        synchronized (LISTA) {
            Funcionario f = LISTA.stream()
                    .filter(x -> x.idFuncionario().equals(idFuncionario))
                    .findFirst().orElseThrow();
            Funcionario novo = new Funcionario(f.idFuncionario(), f.nome(),
                    f.idUsuario(), f.cpf(), f.rg(), f.telefone(), f.ctps(),
                    f.dependente(), f.sexo(), f.salario(), f.dataNascimento(),
                    f.estadoCivil(), ativo);
            LISTA.replaceAll(x -> x == f ? novo : x);
            return novo;
        }
    }

    /**
     * Recusa CPF repetido, como a API.
     *
     * @param f funcionário a gravar
     */
    private void verificarCpf(Funcionario f) {
        boolean repetido = LISTA.stream().anyMatch(x ->
                x.cpf().equals(f.cpf())
                && !x.idFuncionario().equals(f.idFuncionario()));
        if (repetido) {
            throw new ServiceException("Já existe um funcionário com esse CPF");
        }
    }
}
