package com.lojaroupas.service.mock;

import com.lojaroupas.model.Peca;
import com.lojaroupas.service.PecaService;
import com.lojaroupas.service.ServiceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Peças falsas em memória, para o modo de teste.
 */
public class PecaServiceMock implements PecaService {

    /** Acervo de exemplo, que pode ser alterado durante o uso. */
    private static final List<Peca> ACERVO = new ArrayList<>(List.of(
            new Peca(1, "TRJ-001", "Terno Slim Fit Completo", "M",
                    "Preto", "Lã Fria", new BigDecimal("250.00")),
            new Peca(2, "TRJ-002", "Vestido de Gala Sereia", "P",
                    "Vermelho", "Seda", new BigDecimal("380.00")),
            new Peca(3, "TRJ-003", "Smoking Tradicional", "G",
                    "Azul Marinho", "Poliéster", new BigDecimal("300.00"))));

    /** Próximo identificador livre. */
    private static int proximoId = 4;

    @Override
    public List<Peca> listar() {
        SimuladorRede.aguardar();
        synchronized (ACERVO) {
            return List.copyOf(ACERVO);
        }
    }

    @Override
    public Peca buscarPorCodigo(String codigo) {
        SimuladorRede.aguardar();
        synchronized (ACERVO) {
            return ACERVO.stream()
                    .filter(p -> p.codigoUnico().equalsIgnoreCase(codigo.trim()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(
                            "Produto não encontrado com esse código"));
        }
    }

    @Override
    public Peca salvar(Peca peca) {
        SimuladorRede.aguardar();
        synchronized (ACERVO) {
            boolean repetido = ACERVO.stream().anyMatch(p ->
                    p.codigoUnico().equalsIgnoreCase(peca.codigoUnico())
                    && !p.idPeca().equals(peca.idPeca()));
            if (repetido) {
                throw new ServiceException(
                        "Já existe um produto com esse código");
            }
            if (peca.idPeca() == null) {
                Peca nova = new Peca(proximoId++, peca.codigoUnico(),
                        peca.descricao(), peca.tamanho(), peca.cor(),
                        peca.material(), peca.preco());
                ACERVO.add(nova);
                return nova;
            }
            ACERVO.replaceAll(p -> p.idPeca().equals(peca.idPeca())
                    ? peca : p);
            return peca;
        }
    }

    @Override
    public void excluir(Integer idPeca) {
        SimuladorRede.aguardar();
        synchronized (ACERVO) {
            ACERVO.removeIf(p -> p.idPeca().equals(idPeca));
        }
    }
}
