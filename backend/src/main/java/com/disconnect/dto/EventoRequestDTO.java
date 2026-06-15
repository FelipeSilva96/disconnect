package com.disconnect.dto;

import java.util.List;

import com.disconnect.domain.enums.FrequenciaEvento;

public class EventoRequestDTO {

    private String nome;
    private String descricao;
    private String dataEvento;
    private String local;
    private Double latitude;
    private Double longitude;
    private String cidade;
    private FrequenciaEvento frequencia;
    private List<Integer> categoriaIds;
    private List<Integer> modalidadeIds;
    private String fotoUrl;
    private List<String> diasDaSemana;
    private List<Integer> diasDoMes;
    private Integer quantMinimaPessoas;
    private Integer quantMaximaPessoas;
    private Integer participantesInscritos;
    private String modalidadeHobby;
    private String nivelDeHabilidade;
    private String status;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
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

    public List<Integer> getModalidadeIds() {
        return modalidadeIds != null ? modalidadeIds : categoriaIds;
    }

    public void setModalidadeIds(List<Integer> modalidadeIds) {
        this.modalidadeIds = modalidadeIds;
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

    public Integer getParticipantesInscritos() {
        return participantesInscritos;
    }

    public void setParticipantesInscritos(Integer participantesInscritos) {
        this.participantesInscritos = participantesInscritos;
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
}