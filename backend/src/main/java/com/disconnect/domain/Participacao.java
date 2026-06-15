package com.disconnect.domain;

import java.time.LocalDateTime;

import com.disconnect.domain.enums.StatusParticipacao;

public class Participacao {

    private Usuario solicitante; // Quem está pedindo para entrar
    private Evento evento; // Em qual evento
    private StatusParticipacao status;
    private LocalDateTime dataSolicitacao;
    private String mensagemSolicitacao;
    private String mensagemResposta;
    private LocalDateTime dataResposta;

    public Participacao() {
    }

    public Participacao(Usuario solicitante, Evento evento, String mensagemSolicitacao) {
        this.solicitante = solicitante;
        this.evento = evento;
        this.status = StatusParticipacao.PENDENTE; // Status padrão ao criar
        this.dataSolicitacao = LocalDateTime.now();
        this.mensagemSolicitacao = mensagemSolicitacao;
    }

    public void aprovar(String mensagemResposta) {
        this.status = StatusParticipacao.APROVADO;
        this.mensagemResposta = mensagemResposta;
        this.dataResposta = LocalDateTime.now();
    }

    public void recusar(String mensagemResposta) {
        this.status = StatusParticipacao.RECUSADO;
        this.mensagemResposta = mensagemResposta;
        this.dataResposta = LocalDateTime.now();
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public StatusParticipacao getStatus() {
        return status;
    }

    public void setStatus(StatusParticipacao status) {
        this.status = status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getMensagemSolicitacao() {
        return mensagemSolicitacao;
    }

    public void setMensagemSolicitacao(String mensagemSolicitacao) {
        this.mensagemSolicitacao = mensagemSolicitacao;
    }

    public String getMensagemResposta() {
        return mensagemResposta;
    }

    public void setMensagemResposta(String mensagemResposta) {
        this.mensagemResposta = mensagemResposta;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

}
