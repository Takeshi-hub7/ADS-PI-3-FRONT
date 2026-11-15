package com.lojaroupas.service.mock;

import com.lojaroupas.model.Locacao;
import com.lojaroupas.service.LocacaoService;
import com.lojaroupas.service.ServiceException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Locações falsas em memória, para o modo de teste. Aplica a mesma
 * regra de bloqueio da API: a peça fica ocupada do dia do evento até o
 * dia da devolução, inclusive.
 */
public class LocacaoServiceMock implements LocacaoService {

    /** Locações registradas nesta execução. */
    private final List<Locacao> base = new ArrayList<>();

    /** Próximo identificador a usar. */
    private int proximoId = 1;

    @Override
    public synchronized List<Locacao> listar() {
        SimuladorRede.aguardar();
        return List.copyOf(base);
    }

    @Override
    public synchronized Locacao criar(Locacao locacao) {
        SimuladorRede.aguardar();
        LocalDate inicio = locacao.dataEvento();
        LocalDate fim = locacao.dataDevolucao() != null
                ? locacao.dataDevolucao() : inicio;
        for (Integer idPeca : locacao.idsPecas()) {
            for (Locacao existente : base) {
                LocalDate fimExistente = existente.dataDevolucao() != null
                        ? existente.dataDevolucao()
                        : existente.dataEvento();
                boolean sobrepoe = !existente.dataEvento().isAfter(fim)
                        && !fimExistente.isBefore(inicio);
                if (sobrepoe && existente.idsPecas().contains(idPeca)) {
                    throw new ServiceException("A peça " + idPeca
                            + " já está locada no período informado");
                }
            }
        }
        Locacao nova = new Locacao(proximoId++, locacao.idCliente(),
                locacao.idUsuario(), locacao.idFuncionario(), inicio,
                locacao.dataDevolucao(), List.copyOf(locacao.idsPecas()));
        base.add(nova);
        return nova;
    }

    @Override
    public synchronized void excluir(Integer idLocacao) {
        SimuladorRede.aguardar();
        base.removeIf(l -> l.idLocacao().equals(idLocacao));
    }
}
