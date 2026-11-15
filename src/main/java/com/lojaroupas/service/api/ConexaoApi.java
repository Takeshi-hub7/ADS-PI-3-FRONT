package com.lojaroupas.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lojaroupas.service.ServiceException;
import com.lojaroupas.util.Configuracao;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

/**
 * Comunicação HTTP com a API do TRAJELOC.
 *
 * <p>Monta as requisições a partir do endereço configurado em
 * {@code api.url}, converte objetos para JSON e traduz as respostas de
 * erro da API em {@link ServiceException} com a mensagem que ela
 * devolveu, para a tela exibir.</p>
 */
final class ConexaoApi {

    /** Mensagem quando o servidor não responde. */
    private static final String MSG_SEM_CONEXAO =
            "Não foi possível conectar ao servidor. Verifique se a API "
            + "está ligada e se o endereço da configuração está correto.";

    /** Mensagem quando o servidor demora demais. */
    private static final String MSG_TEMPO_ESGOTADO =
            "O servidor demorou para responder. Tente novamente.";

    /** Mensagem quando a API devolve um erro sem texto. */
    private static final String MSG_ERRO_GENERICO =
            "O servidor recusou a operação (código %d).";

    /** Cliente HTTP compartilhado. */
    private final HttpClient http;

    /** Conversor entre objetos e JSON. */
    private final ObjectMapper json;

    /** Endereço base da API, sem barra no final. */
    private final String urlBase;

    /** Tempo máximo de espera por resposta. */
    private final Duration timeout;

    /** Cria a conexão com os valores da configuração. */
    ConexaoApi() {
        this.urlBase = Configuracao.getUrlApi();
        this.timeout = Duration.ofSeconds(Configuracao.getTimeoutSegundos());
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Retorna o conversor JSON usado pela conexão.
     *
     * @return conversor configurado para datas do java.time
     */
    ObjectMapper json() {
        return json;
    }

    /**
     * Envia uma requisição e devolve o corpo da resposta como JSON.
     *
     * @param metodo método HTTP (GET, POST, PUT ou DELETE)
     * @param caminho caminho da rota, começando com barra
     * @param corpo objeto enviado como JSON, ou {@code null}
     * @return corpo da resposta, ou {@code null} se vier vazio
     * @throws ServiceException se a API recusar ou não responder
     */
    JsonNode enviar(String metodo, String caminho, Object corpo) {
        HttpRequest requisicao = montar(metodo, caminho, corpo);
        HttpResponse<String> resposta;
        try {
            resposta = http.send(requisicao,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new ServiceException(MSG_TEMPO_ESGOTADO, e);
        } catch (ConnectException e) {
            throw new ServiceException(MSG_SEM_CONEXAO, e);
        } catch (IOException e) {
            throw new ServiceException(MSG_SEM_CONEXAO, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(MSG_SEM_CONEXAO, e);
        }
        String texto = resposta.body();
        JsonNode no = lerJson(texto);
        if (resposta.statusCode() >= 400) {
            throw new ServiceException(
                    extrairMensagem(no, resposta.statusCode()));
        }
        return no;
    }

    /**
     * Monta a requisição HTTP.
     *
     * @param metodo método HTTP
     * @param caminho caminho da rota
     * @param corpo objeto do corpo, ou {@code null}
     * @return requisição pronta para envio
     */
    private HttpRequest montar(String metodo, String caminho, Object corpo) {
        HttpRequest.Builder construtor = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + caminho))
                .timeout(timeout)
                .header("Accept", "application/json");
        if (corpo == null) {
            return construtor.method(metodo,
                    HttpRequest.BodyPublishers.noBody()).build();
        }
        try {
            String texto = json.writeValueAsString(corpo);
            return construtor.header("Content-Type", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(
                            texto, StandardCharsets.UTF_8))
                    .build();
        } catch (JsonProcessingException e) {
            throw new ServiceException("Falha ao preparar os dados.", e);
        }
    }

    /**
     * Converte o texto da resposta em JSON.
     *
     * @param texto corpo da resposta
     * @return árvore JSON, ou {@code null} se o corpo estiver vazio
     *         ou não for JSON
     */
    private JsonNode lerJson(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return json.readTree(texto);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Extrai a mensagem de erro devolvida pela API.
     *
     * <p>A API usa dois formatos: {@code {"erro": "..."}} e, nos erros
     * de validação, um campo por chave ({@code {"telefone": "..."}}).</p>
     *
     * @param no corpo da resposta de erro
     * @param status código HTTP da resposta
     * @return mensagem para exibir ao usuário
     */
    private String extrairMensagem(JsonNode no, int status) {
        if (no != null && no.hasNonNull("erro")) {
            return no.get("erro").asText();
        }
        if (no != null && no.isObject() && no.size() > 0) {
            StringBuilder texto = new StringBuilder();
            Iterator<Map.Entry<String, JsonNode>> campos = no.fields();
            while (campos.hasNext()) {
                Map.Entry<String, JsonNode> campo = campos.next();
                if (texto.length() > 0) {
                    texto.append('\n');
                }
                texto.append(campo.getKey()).append(": ")
                        .append(campo.getValue().asText());
            }
            return texto.toString();
        }
        return String.format(MSG_ERRO_GENERICO, status);
    }
}
