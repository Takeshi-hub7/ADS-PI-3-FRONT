package com.lojaroupas.service.mock;

import com.lojaroupas.model.Peca;
import com.lojaroupas.service.PecaService;
import java.math.BigDecimal;
import java.util.List;

/**
 * Peças falsas em memória, para o modo de teste.
 */
public class PecaServiceMock implements PecaService {

    /** Acervo de exemplo. */
    private static final List<Peca> ACERVO = List.of(
            new Peca(1, "TRJ-001", "Terno Slim Fit Completo", "M",
                    "Preto", new BigDecimal("250.00")),
            new Peca(2, "TRJ-002", "Vestido de Gala Sereia", "P",
                    "Vermelho", new BigDecimal("380.00")),
            new Peca(3, "TRJ-003", "Smoking Tradicional", "G",
                    "Azul Marinho", new BigDecimal("300.00")));

    @Override
    public List<Peca> listar() {
        SimuladorRede.aguardar();
        return ACERVO;
    }
}
