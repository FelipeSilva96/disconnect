package com.disconnect.dto;

public class ParticipacaoRequestDTO {

    private Integer id_solicitante;
    private Integer id_evento;
    private String mensagem_solicitacao;

    public Integer getId_solicitante() {
        return id_solicitante;
    }

    public void setId_solicitante(Integer id_solicitante) {
        this.id_solicitante = id_solicitante;
    }

    public Integer getId_evento() {
        return id_evento;
    }

    public void setId_evento(Integer id_evento) {
        this.id_evento = id_evento;
    }

    public String getMensagem_solicitacao() {
        return mensagem_solicitacao;
    }

    public void setMensagem_solicitacao(String mensagem_solicitacao) {
        this.mensagem_solicitacao = mensagem_solicitacao;
    }
}
