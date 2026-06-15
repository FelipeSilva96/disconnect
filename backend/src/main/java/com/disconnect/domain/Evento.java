package com.disconnect.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.disconnect.domain.enums.FrequenciaEvento;

public class Evento {

    private Integer id;
    private String nome;
    private String descricao;
    private LocalDateTime dataEvento;
    private String localizacao;
    private Double latitude;
    private Double longitude;
    private String cidade;
    private FrequenciaEvento frequencia;
    private String urlFoto;
    private List<String> diasDaSemana;
    private List<Integer> diasDoMes;
    private Integer quantMinimaPessoas;
    private Integer quantMaximaPessoas;
    private String nivelDeHabilidade;
    private String status;

    private Usuario organizador;

    private List<Modalidade> modalidades;

    private LocalDateTime dataCriacao;

    public Evento() {
        this.diasDaSemana = new ArrayList<>();
        this.diasDoMes = new ArrayList<>();
        this.modalidades = new ArrayList<>();
    }

    public Evento(Integer id, String nome, LocalDateTime dataEvento, String localizacao, FrequenciaEvento frequencia, Usuario organizador) {
        this();
        this.id = id;
        this.nome = nome;
        this.dataEvento = dataEvento;
        this.localizacao = localizacao;
        this.frequencia = frequencia;
        this.organizador = organizador;
        this.status = "Ativo";
        this.dataCriacao = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDateTime dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
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

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public List<String> getDiasDaSemana() {
        return diasDaSemana;
    }

    public void setDiasDaSemana(List<String> diasDaSemana) {
        this.diasDaSemana = diasDaSemana != null ? diasDaSemana : new ArrayList<>();
    }

    public List<Integer> getDiasDoMes() {
        return diasDoMes;
    }

    public void setDiasDoMes(List<Integer> diasDoMes) {
        this.diasDoMes = diasDoMes != null ? diasDoMes : new ArrayList<>();
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

    public Usuario getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Usuario organizador) {
        this.organizador = organizador;
    }

    public List<Modalidade> getModalidades() {
        return modalidades;
    }

    public void setModalidades(List<Modalidade> modalidades) {
        this.modalidades = modalidades != null ? modalidades : new ArrayList<>();
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

}
