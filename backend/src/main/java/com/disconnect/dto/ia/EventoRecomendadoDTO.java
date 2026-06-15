package com.disconnect.dto.ia;

import java.util.ArrayList;
import java.util.List;

import com.disconnect.dto.EventoResponseDTO;

public class EventoRecomendadoDTO {

    private EventoResponseDTO evento;
    private Integer pontuacao;
    private String motivo;
    private List<String> sinais;

    public EventoRecomendadoDTO() {
        this.sinais = new ArrayList<>();
    }

    public EventoRecomendadoDTO(EventoResponseDTO evento, Integer pontuacao, String motivo, List<String> sinais) {
        this.evento = evento;
        this.pontuacao = pontuacao;
        this.motivo = motivo;
        this.sinais = sinais != null ? sinais : new ArrayList<>();
    }

    public EventoResponseDTO getEvento() {
        return evento;
    }

    public void setEvento(EventoResponseDTO evento) {
        this.evento = evento;
    }

    public Integer getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Integer pontuacao) {
        this.pontuacao = pontuacao;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public List<String> getSinais() {
        return sinais;
    }

    public void setSinais(List<String> sinais) {
        this.sinais = sinais;
    }
}