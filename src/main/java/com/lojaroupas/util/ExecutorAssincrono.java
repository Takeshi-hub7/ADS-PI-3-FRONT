package com.lojaroupas.util;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

/**
 * Executa trabalhos demorados (como chamadas de rede) fora da thread da
 * interface, para que a tela não congele.
 *
 * <p>Os retornos de sucesso e de falha são executados na thread da
 * interface, portanto podem alterar componentes visuais com segurança.</p>
 */
public final class ExecutorAssincrono {

    /** Nome das threads criadas, útil para depuração. */
    private static final String NOME_THREAD = "tarefa-assincrona";

    /** Mensagem usada quando o erro não possui texto. */
    private static final String MENSAGEM_PADRAO = "Erro inesperado.";

    /** Impede a criação de instâncias da classe utilitária. */
    private ExecutorAssincrono() {
    }

    /**
     * Executa uma tarefa que produz um resultado.
     *
     * @param <T> tipo do resultado
     * @param tarefa trabalho a executar em segundo plano
     * @param aoConcluir chamado com o resultado, em caso de sucesso
     * @param aoFalhar chamado com o erro, em caso de falha
     */
    public static <T> void executar(Supplier<T> tarefa,
            Consumer<T> aoConcluir, Consumer<Throwable> aoFalhar) {
        Task<T> trabalho = new Task<>() {
            @Override
            protected T call() {
                return tarefa.get();
            }
        };
        trabalho.setOnSucceeded(
                evento -> aoConcluir.accept(trabalho.getValue()));
        trabalho.setOnFailed(
                evento -> aoFalhar.accept(trabalho.getException()));
        iniciar(trabalho);
    }

    /**
     * Executa uma tarefa que não produz resultado (por exemplo, excluir).
     *
     * @param tarefa trabalho a executar em segundo plano
     * @param aoConcluir chamado em caso de sucesso
     * @param aoFalhar chamado com o erro, em caso de falha
     */
    public static void executarSemRetorno(Runnable tarefa,
            Runnable aoConcluir, Consumer<Throwable> aoFalhar) {
        Task<Void> trabalho = new Task<>() {
            @Override
            protected Void call() {
                tarefa.run();
                return null;
            }
        };
        trabalho.setOnSucceeded(evento -> aoConcluir.run());
        trabalho.setOnFailed(
                evento -> aoFalhar.accept(trabalho.getException()));
        iniciar(trabalho);
    }

    /**
     * Obtém um texto amigável para exibir ao usuário a partir de um erro.
     *
     * @param erro erro ocorrido, possivelmente {@code null}
     * @return mensagem do erro ou um texto padrão
     */
    public static String obterMensagem(Throwable erro) {
        if (erro != null && erro.getMessage() != null) {
            return erro.getMessage();
        }
        return MENSAGEM_PADRAO;
    }

    /**
     * Inicia a tarefa em uma thread própria.
     *
     * @param trabalho tarefa a executar
     */
    private static void iniciar(Task<?> trabalho) {
        Thread thread = new Thread(trabalho, NOME_THREAD);
        thread.setDaemon(true);
        thread.start();
    }
}