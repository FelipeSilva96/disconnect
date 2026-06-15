package com.disconnect.dto.ia;

import java.util.ArrayList;
import java.util.List;

public class RecomendacaoEventoResponseDTO {

    private Boolean geradoPorIa;
    private String mensagem;
    private List<EventoRecomendadoDTO> recomendacoes;

    public RecomendacaoEventoResponseDTO() {
        this.recomendacoes = new ArrayList<>();
    }

    public RecomendacaoEventoResponseDTO(Boolean geradoPorIa, String mensagem,
            List<EventoRecomendadoDTO> recomendacoes) {
        this.geradoPorIa = geradoPorIa;
        this.mensagem = mensagem;
        this.recomendacoes = recomendacoes != null ? recomendacoes : new ArrayList<>();
    }

    public Boolean getGeradoPorIa() {
        return geradoPorIa;
    }

    public void setGeradoPorIa(Boolean geradoPorIa) {
        this.geradoPorIa = geradoPorIa;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<EventoRecomendadoDTO> getRecomendacoes() {
        return recomendacoes;
    }

    public void setRecomendacoes(List<EventoRecomendadoDTO> recomendacoes) {
        this.recomendacoes = recomendacoes;
    }
}