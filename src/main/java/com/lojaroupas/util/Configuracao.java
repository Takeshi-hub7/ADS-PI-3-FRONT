package com.lojaroupas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuração da aplicação: modo de operação e endereço da API.
 *
 * <p>Os valores são lidos em duas etapas. Primeiro, o arquivo padrão
 * {@code /config/aplicacao.properties}, que acompanha o programa. Depois,
 * o arquivo opcional {@code ~/.trajeloc/aplicacao.properties}, que cada
 * máquina pode criar para sobrescrever os valores (por exemplo, para
 * apontar para o servidor da loja) sem recompilar o projeto.</p>
 */
public final class Configuracao {

    /** Modo em que os serviços usam dados falsos em memória. */
    public static final String MODO_MOCK = "mock";

    /** Modo em que os serviços usam a API real. */
    public static final String MODO_API = "api";

    /** Registro de mensagens de diagnóstico. */
    private static final Logger REGISTRO =
            Logger.getLogger(Configuracao.class.getName());

    /** Caminho (no classpath) do arquivo de configuração padrão. */
    private static final String RECURSO_PADRAO =
            "/config/aplicacao.properties";

    /** Pasta, dentro da pasta do usuário, do arquivo de sobrescrita. */
    private static final String PASTA_USUARIO = ".trajeloc";

    /** Nome do arquivo de sobrescrita. */
    private static final String ARQUIVO_USUARIO = "aplicacao.properties";

    /** Chave que define o modo de operação. */
    private static final String CHAVE_MODO = "servico.modo";

    /** Chave que define o endereço da API. */
    private static final String CHAVE_URL = "api.url";

    /** Chave que define o tempo máximo de espera, em segundos. */
    private static final String CHAVE_TIMEOUT = "api.timeout.segundos";

    /** Endereço usado quando nenhum é configurado. */
    private static final String URL_PADRAO = "http://localhost:8080";

    /** Tempo de espera usado quando nenhum é configurado, em segundos. */
    private static final int TIMEOUT_PADRAO_SEGUNDOS = 10;

    /** Valores carregados, preenchidos uma única vez. */
    private static final Properties PROPRIEDADES = carregar();

    /** Impede a criação de instâncias da classe utilitária. */
    private Configuracao() {
    }

    /**
     * Retorna o modo de operação configurado.
     *
     * @return {@code "mock"} ou {@code "api"}, em minúsculas
     */
    public static String getModo() {
        return PROPRIEDADES.getProperty(CHAVE_MODO, MODO_MOCK)
                .trim().toLowerCase();
    }

    /**
     * Informa se a aplicação deve usar dados falsos.
     *
     * @return {@code true} se o modo configurado é mock
     */
    public static boolean isModoMock() {
        return MODO_MOCK.equals(getModo());
    }

    /**
     * Retorna o endereço base da API, sem barra no final.
     *
     * @return endereço da API, como {@code http://192.168.0.10:8080}
     */
    public static String getUrlApi() {
        String url = PROPRIEDADES.getProperty(CHAVE_URL, URL_PADRAO).trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Retorna o tempo máximo de espera por uma resposta da API.
     *
     * @return tempo em segundos; o valor padrão se o configurado for
     *         inválido
     */
    public static int getTimeoutSegundos() {
        String texto = PROPRIEDADES.getProperty(CHAVE_TIMEOUT);
        if (texto == null) {
            return TIMEOUT_PADRAO_SEGUNDOS;
        }
        try {
            int segundos = Integer.parseInt(texto.trim());
            return segundos > 0 ? segundos : TIMEOUT_PADRAO_SEGUNDOS;
        } catch (NumberFormatException e) {
            return TIMEOUT_PADRAO_SEGUNDOS;
        }
    }

    /**
     * Descreve a origem dos dados, para exibição na interface.
     *
     * @return texto como "Modo de teste (sem API)" ou "Servidor: ..."
     */
    public static String descreverServidor() {
        if (isModoMock()) {
            return "Modo de teste (sem API)";
        }
        return "Servidor: " + getUrlApi();
    }

    /**
     * Carrega o arquivo padrão e, se existir, o arquivo do usuário.
     *
     * @return propriedades combinadas
     */
    private static Properties carregar() {
        Properties propriedades = new Properties();
        lerRecursoPadrao(propriedades);
        lerArquivoDoUsuario(propriedades);
        return propriedades;
    }

    /**
     * Lê o arquivo de configuração que acompanha o programa.
     *
     * @param destino propriedades que recebem os valores lidos
     */
    private static void lerRecursoPadrao(Properties destino) {
        try (InputStream entrada =
                Configuracao.class.getResourceAsStream(RECURSO_PADRAO)) {
            if (entrada == null) {
                REGISTRO.warning("Configuração padrão não encontrada: "
                        + RECURSO_PADRAO + ". Usando valores internos.");
                return;
            }
            lerPropriedades(entrada, destino);
        } catch (IOException e) {
            REGISTRO.log(Level.WARNING,
                    "Falha ao ler a configuração padrão.", e);
        }
    }

    /**
     * Lê o arquivo de sobrescrita da máquina, se ele existir.
     *
     * @param destino propriedades que recebem os valores lidos
     */
    private static void lerArquivoDoUsuario(Properties destino) {
        Path caminho = Paths.get(System.getProperty("user.home"),
                PASTA_USUARIO, ARQUIVO_USUARIO);
        if (!Files.isRegularFile(caminho)) {
            return;
        }
        try (InputStream entrada = Files.newInputStream(caminho)) {
            lerPropriedades(entrada, destino);
        } catch (IOException e) {
            REGISTRO.log(Level.WARNING,
                    "Falha ao ler " + caminho + ".", e);
        }
    }

    /**
     * Lê propriedades de um fluxo, interpretando o texto como UTF-8.
     *
     * @param entrada fluxo de leitura
     * @param destino propriedades que recebem os valores lidos
     * @throws IOException se a leitura falhar
     */
    private static void lerPropriedades(InputStream entrada,
            Properties destino) throws IOException {
        try (Reader leitor = new InputStreamReader(entrada,
                StandardCharsets.UTF_8)) {
            destino.load(leitor);
        }
    }
}