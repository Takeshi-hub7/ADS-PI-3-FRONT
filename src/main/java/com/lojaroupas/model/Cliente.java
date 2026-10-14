package com.lojaroupas.model;

import java.time.LocalDate;

/**
 * Cliente da loja, como a interface o enxerga.
 *
 * <p>Os atributos seguem os campos descritos na documentação do back-end.
 * Ajustar quando {@code ClienteRequest} e {@code ClienteResponse}
 * forem confirmados.</p>
 */
public class Cliente {

    /** Identificador gerado pelo banco; {@code null} se ainda não salvo. */
    private Integer idCliente;

    /** Nome completo (obrigatório, até 60 caracteres). */
    private String nome = "";

    /** CPF do cliente (obrigatório). */
    private String cpf = "";

    /** RG do cliente (opcional). */
    private String rg = "";

    /** Data de nascimento (opcional). */
    private LocalDate dataNascimento;

    /** Medidas do cliente, em texto livre (opcional). */
    private String medidas = "";

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
}