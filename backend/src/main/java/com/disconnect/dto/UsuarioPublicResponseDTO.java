package com.disconnect.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.disconnect.domain.Usuario;

public class UsuarioPublicResponseDTO {

    private Integer id;
    private String nome;
    private String login;
    private Integer idade;
    private String biografia;
    private String urlFoto;
    private String localizacao;
    private List<String> hobbies;
    private Map<String, String> nivelExperiencia;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private LocalDateTime dataCriacao;

    public UsuarioPublicResponseDTO() {
    }

    public UsuarioPublicResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.idade = usuario.getIdade();
        this.biografia = usuario.getBiografia();
        this.urlFoto = usuario.getUrlFoto();
        this.localizacao = usuario.getLocalizacao();
        this.hobbies = usuario.getHobbies();
        this.nivelExperiencia = usuario.getNivelExperiencia();
        this.avaliacaoMedia = usuario.getAvaliacaoMedia();
        this.totalAvaliacoes = usuario.getTotalAvaliacoes();
        this.dataCriacao = usuario.getDataCriacao();
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public Integer getIdade() {
        return idade;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public Map<String, String> getNivelExperiencia() {
        return nivelExperiencia;
    }

    public Double getAvaliacaoMedia() {
        return avaliacaoMedia;
    }

    public Integer getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}