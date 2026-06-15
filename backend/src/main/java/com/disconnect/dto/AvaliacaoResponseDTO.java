package com.disconnect.dto;

import java.time.LocalDateTime;

import com.disconnect.domain.Avaliacao;

public class AvaliacaoResponseDTO {

    private Integer id;
    private Integer nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;
    private Integer avaliadorId;
    private Integer avaliadoId;
    private Integer eventoId;

    public AvaliacaoResponseDTO() {
    }

    public AvaliacaoResponseDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.nota = avaliacao.getNota();
        this.comentario = avaliacao.getComentario();
        this.dataAvaliacao = avaliacao.getDataAvaliacao();

        if (avaliacao.getAvaliador() != null) {
            this.avaliadorId = avaliacao.getAvaliador().getId();
        }

        if (avaliacao.getAvaliado() != null) {
            this.avaliadoId = avaliacao.getAvaliado().getId();
        }

        if (avaliacao.getEvento() != null) {
            this.eventoId = avaliacao.getEvento().getId();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public Integer getAvaliadorId() {
        return avaliadorId;
    }

    public Integer getAvaliadoId() {
        return avaliadoId;
    }

    public void setAvaliadorId(Integer avaliadorId) {
        this.avaliadorId = avaliadorId;
    }

    public void setAvaliadoId(Integer avaliadoId) {
        this.avaliadoId = avaliadoId;
    }

    public Integer getEventoId() {
        return eventoId;
    }

    public void setEventoId(Integer eventoId) {
        this.eventoId = eventoId;
    }
}
