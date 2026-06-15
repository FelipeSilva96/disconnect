package com.disconnect.dto.ia;

import java.util.ArrayList;
import java.util.List;

import com.disconnect.domain.enums.FrequenciaEvento;

public class RascunhoEventoIaResponseDTO {

    private String nome;
    private String descricao;
    private String dataEvento;
    private String local;
    private FrequenciaEvento frequencia;
    private List<Integer> categoriaIds;
    private String fotoUrl;
    private List<String> diasDaSemana;
    private List<Integer> diasDoMes;
    private Integer quantMinimaPessoas;
    private Integer quantMaximaPessoas;
    private String modalidadeHobby;
    private String nivelDeHabilidade;
    private String status;
    private List<String> alertas;
    private Boolean geradoPorIa;

    public RascunhoEventoIaResponseDTO() {
        this.categoriaIds = new ArrayList<>();
        this.diasDaSemana = new ArrayList<>();
        this.diasDoMes = new ArrayList<>();
        this.alertas = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(String dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public FrequenciaEvento getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(FrequenciaEvento frequencia) {
        this.frequencia = frequencia;
    }

    public List<Integer> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(List<Integer> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public List<String> getDiasDaSemana() {
        return diasDaSemana;
    }

    public void setDiasDaSemana(List<String> diasDaSemana) {
        this.diasDaSemana = diasDaSemana;
    }

    public List<Integer> getDiasDoMes() {
        return diasDoMes;
    }

    public void setDiasDoMes(List<Integer> diasDoMes) {
        this.diasDoMes = diasDoMes;
    }

    public Integer getQuantMinimaPessoas() {
        return quantMinimaPessoas;
    }

    public void setQuantMinimaPessoas(Integer quantMinimaPessoas) {
        this.quantMinimaPessoas = quantMinimaPessoas;
    }

    public Integer getQuantMaximaPessoas() {
        return quantMaximaPessoas;
    }

    public void setQuantMaximaPessoas(Integer quantMaximaPessoas) {
        this.quantMaximaPessoas = quantMaximaPessoas;
    }

    public String getModalidadeHobby() {
        return modalidadeHobby;
    }

    public void setModalidadeHobby(String modalidadeHobby) {
        this.modalidadeHobby = modalidadeHobby;
    }

    public String getNivelDeHabilidade() {
        return nivelDeHabilidade;
    }

    public void setNivelDeHabilidade(String nivelDeHabilidade) {
        this.nivelDeHabilidade = nivelDeHabilidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<String> alertas) {
        this.alertas = alertas;
    }

    public Boolean getGeradoPorIa() {
        return geradoPorIa;
    }

    public void setGeradoPorIa(Boolean geradoPorIa) {
        this.geradoPorIa = geradoPorIa;
    }
}