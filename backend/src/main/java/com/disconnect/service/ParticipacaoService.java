package com.disconnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import com.disconnect.dao.EventoDAO;
import com.disconnect.dao.ParticipacaoDAO;
import com.disconnect.dao.UsuarioDAO;
import com.disconnect.domain.Evento;
import com.disconnect.domain.Participacao;
import com.disconnect.domain.Usuario;
import com.disconnect.domain.enums.StatusParticipacao;

public class ParticipacaoService {

    private final EventoDAO eventoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ParticipacaoDAO participacaoDAO;

    public ParticipacaoService() {
        this.eventoDAO = new EventoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.participacaoDAO = new ParticipacaoDAO();
    }

    public Participacao solicitarParticipacao(Integer idSolicitante, Integer idEvento, String mensagemSolicitacao) {
        Usuario usuario = buscarUsuario(idSolicitante);
        Evento evento = buscarEvento(idEvento);

        validarPerfilParaParticipacao(usuario);

        if (evento.getDataEvento().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Nao e possivel solicitar participacao em eventos passados.");
        }

        if (evento.getOrganizador() != null && evento.getOrganizador().getId().equals(idSolicitante)) {
            throw new IllegalStateException("Organizador nao pode solicitar participacao em seu proprio evento.");
        }

        if (participacaoDAO.buscarPorUsuarioEEvento(idSolicitante, idEvento) != null) {
            throw new IllegalStateException("Usuario ja solicitou participacao neste evento.");
        }

        Participacao participacao = new Participacao(usuario, evento, trimToNull(mensagemSolicitacao));

        return participacaoDAO.inserir(participacao);
    }

    public List<Participacao> listarPorEvento(Integer idEvento) {
        buscarEvento(idEvento);
        return participacaoDAO.listarPorEvento(idEvento);
    }

    public List<Participacao> listarPorUsuario(Integer idUsuario) {
        buscarUsuario(idUsuario);
        return participacaoDAO.listarPorUsuario(idUsuario);
    }

    public boolean deletar(Integer idEvento, Integer idSolicitante) {
        Participacao participacao = buscarParticipacao(idSolicitante, idEvento);

        if (participacao.getStatus() == StatusParticipacao.APROVADO) {
            throw new IllegalStateException("Nao e possivel deletar participacao aprovada.");
        }

        return participacaoDAO.deletar(idEvento, idSolicitante);
    }

    public Participacao atualizarMensagem(Integer idEvento, Integer idSolicitante, String mensagemSolicitacao) {
        Participacao participacaoExistente = buscarParticipacao(idSolicitante, idEvento);

        if (participacaoExistente.getStatus() != StatusParticipacao.PENDENTE) {
            throw new IllegalStateException("Nao e possivel atualizar participacao respondida.");
        }

        participacaoExistente.setMensagemSolicitacao(trimToNull(mensagemSolicitacao));
        return participacaoDAO.atualizar(participacaoExistente);
    }

    public Participacao responderSolicitacao(Integer idEvento, Integer idSolicitante, StatusParticipacao status,
            String mensagemResposta) {
        Participacao participacaoExistente = buscarParticipacao(idSolicitante, idEvento);

        if (participacaoExistente.getStatus() != StatusParticipacao.PENDENTE) {
            throw new IllegalStateException("Nao e possivel responder solicitacao ja respondida.");
        }

        if (status == StatusParticipacao.APROVADO) {
            participacaoExistente.aprovar(trimToNull(mensagemResposta));
        } else if (status == StatusParticipacao.RECUSADO) {
            participacaoExistente.recusar(trimToNull(mensagemResposta));
        } else {
            throw new IllegalArgumentException("Status de resposta invalido.");
        }

        return participacaoDAO.responderSolicitacao(participacaoExistente);
    }

    private void validarPerfilParaParticipacao(Usuario usuario) {
        Integer idade = usuario.getIdade();
        if (idade == null || idade < 18) {
            throw new IllegalStateException(
                    "E necessario ter 18 anos ou mais (com data de nascimento informada no perfil) para participar de eventos.");
        }

        boolean perfilCompleto = naoVazio(usuario.getUrlFoto())
                && naoVazio(usuario.getBiografia())
                && naoVazio(usuario.getLocalizacao())
                && usuario.getHobbies() != null && !usuario.getHobbies().isEmpty();

        if (!perfilCompleto) {
            throw new IllegalStateException(
                    "Complete seu perfil (foto, biografia, cidade e hobbies) para solicitar participacao em eventos.");
        }
    }

    private boolean naoVazio(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private Participacao buscarParticipacao(Integer idSolicitante, Integer idEvento) {
        validarId(idSolicitante, "O ID do solicitante fornecido e invalido.");
        validarId(idEvento, "O ID do evento fornecido e invalido.");

        Participacao participacao = participacaoDAO.buscarPorUsuarioEEvento(idSolicitante, idEvento);
        if (participacao == null) {
            throw new NoSuchElementException("Participacao nao encontrada.");
        }

        return participacao;
    }

    private Evento buscarEvento(Integer idEvento) {
        validarId(idEvento, "O ID do evento fornecido e invalido.");

        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) {
            throw new NoSuchElementException("Evento nao encontrado.");
        }

        return evento;
    }

    private Usuario buscarUsuario(Integer idUsuario) {
        validarId(idUsuario, "O ID do usuario fornecido e invalido.");

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new NoSuchElementException("Usuario nao encontrado.");
        }

        return usuario;
    }

    private void validarId(Integer id, String mensagem) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private String trimToNull(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
