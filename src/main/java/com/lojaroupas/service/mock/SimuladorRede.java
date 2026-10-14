package com.lojaroupas.service.mock;

/**
 * Simula a demora de uma chamada de rede, permitindo testar os estados
 * de carregamento da interface.
 */
final class SimuladorRede {

    /** Duração da espera simulada, em milissegundos. */
    private static final long ATRASO_MILISSEGUNDOS = 300L;

    /** Impede a criação de instâncias da classe utilitária. */
    private SimuladorRede() {
    }

    /** Aguarda o tempo configurado, simulando a latência da rede. */
    static void aguardar() {
        try {
            Thread.sleep(ATRASO_MILISSEGUNDOS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}