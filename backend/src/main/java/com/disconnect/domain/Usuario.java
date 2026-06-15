package com.disconnect.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    private Integer id;
    private String nome;
    private String email;
    private String login;
    private String senha;
    @SerializedName(value = "dataNascimento", alternate = { "data_nascimento" })
    private LocalDate dataNascimento;
    private String biografia;
    private String urlFoto;
    private String localizacao;
    private List<String> hobbies;
    private Map<String, String> nivelExperiencia;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private Boolean isAdmin;
    private LocalDateTime dataCriacao;

    public Usuario() {
    }

    public Usuario(Integer id, String nome, String email, String login, String senha, LocalDate dataNascimento,
            String biografia, String urlFoto, String localizacao, List<String> hobbies,
            Map<String, String> nivelExperiencia, Boolean isAdmin) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.login = login;
        this.senha = senha;
        this.dataNascimento = dataNascimento;
        this.biografia = biografia;
        this.urlFoto = urlFoto;
        this.localizacao = localizacao;
        this.hobbies = hobbies;
        this.nivelExperiencia = nivelExperiencia;
        this.isAdmin = isAdmin;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Integer getIdade() {
        if (dataNascimento == null) {
            return null;
        }

        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public Map<String, String> getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(Map<String, String> nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }

    public Double getAvaliacaoMedia() {
        return avaliacaoMedia;
    }

    public void setAvaliacaoMedia(Double avaliacaoMedia) {
        this.avaliacaoMedia = avaliacaoMedia;
    }

    public Integer getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(Integer totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

}
