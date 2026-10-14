package com.lojaroupas.util;

import javafx.scene.Parent;

/**
 * Resultado do carregamento de uma tela FXML.
 *
 * @param <T> tipo do controller associado à tela
 * @param raiz componente raiz da tela, pronto para ser exibido
 * @param controlador controller que gerencia a tela
 */
public record Tela<T>(Parent raiz, T controlador) {
}