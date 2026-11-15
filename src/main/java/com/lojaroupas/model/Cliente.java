package com.lojaroupas.model;

import java.time.LocalDate;

/**
 * Cliente da loja, como a interface o enxerga.
 *
 * <p>Os atributos seguem {@code ClienteRequest} e {@code ClienteResponse}
 * do back-end.</p>
 */
public class Cliente {

    /** Identificador gerado pelo banco; {@code null} se ainda não salvo. */
    private Integer idCliente;

    /** Nome completo (obrigatório, até 60 caracteres). */
    private String nome = "";

    /** CPF do cliente (obrigatório). */
    private String cpf = "";

    /** Telefone de contato (obrigatório, até 15 caracteres). */
    private String telefone = "";

    /** Endereço (obrigatório, até 100 caracteres). */
    private String endereco = "";

    /** RG do cliente (opcional). */
    private String rg = "";

    /** Data de nascimento (opcional). */
    private LocalDate dataNascimento;

    /** Medidas do cliente, em texto livre (opcional). */
    private String medidas = "";

    /**
     * Usuário de acesso vinculado, se houver. Não aparece na tela; é
     * guardado só para ser devolvido à API ao alterar o cliente.
     */
    private Integer idUsuario;

    /**
     * Retorna o identificador do cliente.
     *
     * @return identificador, ou {@code null} se ainda não salvo
     */
    public Integer getIdCliente() {
        return idCliente;
    }

    /**
     * Define o identificador do cliente.
     *
     * @param idCliente identificador gerado pelo banco
     */
    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    /**
     * Retorna o nome do cliente.
     *
     * @return nome completo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do cliente.
     *
     * @param nome nome completo
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna o CPF do cliente.
     *
     * @return CPF, com ou sem formatação
     */
    public String getCpf() {
        return cpf;
    }

    /**
     * Define o CPF do cliente.
     *
     * @param cpf CPF, com ou sem formatação
     */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /**
     * Retorna o RG do cliente.
     *
     * @return RG, ou texto vazio se não informado
     */
    public String getRg() {
        return rg;
    }

    /**
     * Define o RG do cliente.
     *
     * @param rg número do RG
     */
    public void setRg(String rg) {
        this.rg = rg;
    }

    /**
     * Retorna a data de nascimento.
     *
     * @return data de nascimento, ou {@code null} se não informada
     */
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    /**
     * Define a data de nascimento.
     *
     * @param dataNascimento data de nascimento
     */
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    /**
     * Retorna as medidas do cliente.
     *
     * @return medidas em texto livre
     */
    public String getMedidas() {
        return medidas;
    }

    /**
     * Define as medidas do cliente.
     *
     * @param medidas medidas em texto livre
     */
    public void setMedidas(String medidas) {
        this.medidas = medidas;
    }

    /**
     * Retorna o telefone do cliente.
     *
     * @return telefone de contato
     */
    public String getTelefone() {
        return telefone;
    }

    /**
     * Define o telefone do cliente.
     *
     * @param telefone telefone de contato
     */
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /**
     * Retorna o endereço do cliente.
     *
     * @return endereço
     */
    public String getEndereco() {
        return endereco;
    }

    /**
     * Define o endereço do cliente.
     *
     * @param endereco endereço
     */
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    /**
     * Retorna o usuário de acesso vinculado.
     *
     * @return identificador do usuário, ou {@code null}
     */
    public Integer getIdUsuario() {
        return idUsuario;
    }

    /**
     * Define o usuário de acesso vinculado.
     *
     * @param idUsuario identificador do usuário
     */
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}
