package com.lojaroupas.service;

/**
 * Erro de regra de negócio ou de comunicação com o servidor.
 *
 * <p>A mensagem é exibida ao usuário, portanto deve ser clara.</p>
 */
public class ServiceException extends RuntimeException {

    /** Identificador de versão da serialização. */
    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção com uma mensagem.
     *
     * @param mensagem texto explicativo para o usuário
     */
    public ServiceException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria uma exceção com mensagem e causa original.
     *
     * @param mensagem texto explicativo para o usuário
     * @param causa erro original que motivou esta exceção
     */
    public ServiceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}