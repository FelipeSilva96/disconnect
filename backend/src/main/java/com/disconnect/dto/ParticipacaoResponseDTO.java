package com.disconnect.dto;

import java.time.LocalDateTime;

import com.disconnect.domain.Participacao;
import com.disconnect.domain.enums.StatusParticipacao;

public class ParticipacaoResponseDTO {

    private Integer idEvento;
    private Integer idSolicitante;
    private UsuarioPublicResponseDTO solicitante;
    private EventoResponseDTO evento;
    private StatusParticipacao status;
    private LocalDateTime dataSolicitacao;
    private String mensagemSolicitacao;
    private String mensagemResposta;
    private LocalDateTime dataResposta;

    public ParticipacaoResponseDTO(Participacao participacao) {
        if (participacao.getEvento() != null) {
            this.idEvento = participacao.getEvento().getId();
            this.evento = new EventoResponseDTO(participacao.getEvento());
        }

        if (participacao.getSolicitante() != null) {
            this.idSolicitante = participacao.getSolicitante().getId();
            this.solicitante = new UsuarioPublicResponseDTO(participacao.getSolicitante());
        }

        this.status = participacao.getStatus();
        this.dataSolicitacao = participacao.getDataSolicitacao();
        this.mensagemSolicitacao = participacao.getMensagemSolicitacao();
        this.mensagemResposta = participacao.getMensagemResposta();
        this.dataResposta = participacao.getDataResposta();
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public Integer getIdSolicitante() {
        return idSolicitante;
    }

    public UsuarioPublicResponseDTO getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(UsuarioPublicResponseDTO solicitante) {
        this.solicitante = solicitante;
    }

    public EventoResponseDTO getEvento() {
        return evento;
    }

    public void setEvento(EventoResponseDTO evento) {
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
