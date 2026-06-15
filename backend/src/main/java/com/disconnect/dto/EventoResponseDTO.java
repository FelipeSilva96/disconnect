package com.disconnect.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.disconnect.domain.Evento;
import com.disconnect.domain.Modalidade;

public class EventoResponseDTO {

    private Integer id;
    private String nome;
    private String descricao;
    private LocalDateTime dataEvento;
    private String local;
    private Double latitude;
    private Double longitude;
    private String cidade;
    private String frequencia;
    private String fotoUrl;
    private List<String> diasDaSemana;
    private List<Integer> diasDoMes;
    private Integer quantMinimaPessoas;
    private Integer quantMaximaPessoas;
    private Integer participantesInscritos;
    private String modalidadeHobby;
    private String nivelDeHabilidade;
    private String status;
    private UsuarioPublicResponseDTO organizador;
    private List<CategoriaResponseDTO> categorias;
    private LocalDateTime dataCriacao;

    public EventoResponseDTO() {
    }

    public EventoResponseDTO(Evento evento) {
        this.id = evento.getId();
        this.nome = evento.getNome();
        this.descricao = evento.getDescricao();
        this.dataEvento = evento.getDataEvento();
        this.local = evento.getLocalizacao();
        this.latitude = evento.getLatitude();
        this.longitude = evento.getLongitude();
        this.cidade = evento.getCidade();
        this.frequencia = evento.getFrequencia() != null ? evento.getFrequencia().name() : null;
        this.fotoUrl = evento.getUrlFoto();
        this.diasDaSemana = evento.getDiasDaSemana() != null ? new ArrayList<>(evento.getDiasDaSemana())
                : new ArrayList<>();
        this.diasDoMes = evento.getDiasDoMes() != null ? new ArrayList<>(evento.getDiasDoMes()) : new ArrayList<>();
        this.quantMinimaPessoas = evento.getQuantMinimaPessoas();
        this.quantMaximaPessoas = evento.getQuantMaximaPessoas();
        this.participantesInscritos = 0;
        this.modalidadeHobby = evento.getModalidades() != null && !evento.getModalidades().isEmpty()
                ? evento.getModalidades().get(0).getNome()
                : null;
        this.nivelDeHabilidade = evento.getNivelDeHabilidade();
        this.status = evento.getStatus();
        this.organizador = evento.getOrganizador() != null ? new UsuarioPublicResponseDTO(evento.getOrganizador())
                : null;
        this.categorias = new ArrayList<>();
        if (evento.getModalidades() != null) {
            for (Modalidade modalidade : evento.getModalidades()) {
                this.categorias.add(new CategoriaResponseDTO(modalidade));
            }
        }
        this.dataCriacao = evento.getDataCriacao();
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    public String getLocal() {
        return local;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getCidade() {
        return cidade;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public List<String> getDiasDaSemana() {
        return diasDaSemana;
    }

    public List<Integer> getDiasDoMes() {
        return diasDoMes;
    }

    public Integer getQuantMinimaPessoas() {
        return quantMinimaPessoas;
    }

    public Integer getQuantMaximaPessoas() {
        return quantMaximaPessoas;
    }

    public Integer getParticipantesInscritos() {
        return participantesInscritos;
    }

    public String getModalidadeHobby() {
        return modalidadeHobby;
    }

    public String getNivelDeHabilidade() {
        return nivelDeHabilidade;
    }

    public String getStatus() {
        return status;
    }

    public UsuarioPublicResponseDTO getOrganizador() {
        return organizador;
    }

    public List<CategoriaResponseDTO> getCategorias() {
        return categorias;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}