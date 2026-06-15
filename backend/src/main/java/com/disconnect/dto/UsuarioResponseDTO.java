package com.disconnect.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.disconnect.domain.Usuario;

// O DTO iltra dados sensíveis e manda apenas o necessário para o front. É a mesma coisa de Usuario.java só que com atributos a menos
public class UsuarioResponseDTO {

    private Integer id;
    private String nome;
    private String email;
    private String login; // O "@username"
    private LocalDate dataNascimento;
    private Integer idade;
    private String biografia;
    private String urlFoto;
    private String localizacao;
    private List<String> hobbies;
    private Map<String, String> nivelExperiencia;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private LocalDateTime dataCriacao;
    private String token;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario, null);
    }

    public UsuarioResponseDTO(Usuario usuario, String token) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.login = usuario.getLogin();
        this.dataNascimento = usuario.getDataNascimento();
        this.idade = usuario.getIdade();
        this.biografia = usuario.getBiografia();
        this.urlFoto = usuario.getUrlFoto();
        this.localizacao = usuario.getLocalizacao();
        this.hobbies = usuario.getHobbies();
        this.nivelExperiencia = usuario.getNivelExperiencia();
        this.avaliacaoMedia = usuario.getAvaliacaoMedia();
        this.totalAvaliacoes = usuario.getTotalAvaliacoes();
        this.dataCriacao = usuario.getDataCriacao();
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    public Integer getIdade() {
        return idade;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
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

    public String getToken() {
        return token;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public void setNivelExperiencia(Map<String, String> nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }

    public void setAvaliacaoMedia(Double avaliacaoMedia) {
        this.avaliacaoMedia = avaliacaoMedia;
    }

    public void setTotalAvaliacoes(Integer totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
