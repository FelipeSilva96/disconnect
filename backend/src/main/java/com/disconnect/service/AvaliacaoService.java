
package com.disconnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import com.disconnect.dao.AvaliacaoDAO;
import com.disconnect.dao.EventoDAO;
import com.disconnect.domain.Avaliacao;
import com.disconnect.domain.Evento;
import com.disconnect.domain.Usuario;

public class AvaliacaoService {

    private final AvaliacaoDAO avaliacaoDAO;
    private final EventoDAO eventoDAO;

    public AvaliacaoService() {
        this.avaliacaoDAO = new AvaliacaoDAO();
        this.eventoDAO = new EventoDAO();
    }

    public Avaliacao registrarAvaliacao(Avaliacao avaliacao) {

        if (avaliacao.getAvaliador() == null || avaliacao.getAvaliador().getId() == null) {
            throw new IllegalArgumentException(
                    "A avaliação deve obrigatoriamente estar associada a um avaliador (Usuário).");
        }

        if (avaliacao.getAvaliado() == null || avaliacao.getAvaliado().getId() == null) {
            throw new IllegalArgumentException(
                    "A avaliação deve obrigatoriamente estar associada a um usuário avaliado.");
        }

        if (avaliacao.getAvaliador().getId().equals(avaliacao.getAvaliado().getId())) {
            throw new IllegalArgumentException("O usuário não pode avaliar a si mesmo.");
        }

        if (avaliacao.getEvento() == null || avaliacao.getEvento().getId() == null) {
            throw new IllegalArgumentException("A avaliação deve obrigatoriamente estar associada a um Evento.");
        }

        Evento evento = eventoDAO.buscarPorId(avaliacao.getEvento().getId());
        if (evento == null) {
            throw new IllegalArgumentException("Evento não encontrado no sistema.");
        }

        if (evento.getDataEvento() == null || evento.getDataEvento().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Só é possível avaliar eventos já encerrados.");
        }

        avaliacao.setEvento(evento);

        boolean avaliacaoDuplicada = avaliacaoDAO.buscarPorEvento(evento.getId()).stream()
                .anyMatch(avaliacaoExistente -> mesmaAvaliacao(avaliacaoExistente, avaliacao));

        if (avaliacaoDuplicada) {
            throw new IllegalArgumentException("Este usuário já avaliou este participante neste evento.");
        }

        if (avaliacao.getNota() == null || avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new IllegalArgumentException("A nota da avaliação deve ser um valor entre 1 e 5.");
        }

        if (avaliacao.getDataAvaliacao() == null) {
            avaliacao.setDataAvaliacao(LocalDateTime.now());
        }

        if (avaliacao.getComentario() != null) {

            String comentarioLimpo = avaliacao.getComentario().trim();

            if (comentarioLimpo.isEmpty()) {
                avaliacao.setComentario(null);
            } else if (comentarioLimpo.length() > 250) {

                throw new IllegalArgumentException("O comentário não pode exceder 250 caracteres.");
            } else {

                avaliacao.setComentario(comentarioLimpo);
            }
        }

        return avaliacaoDAO.inserir(avaliacao);
    }

    public Avaliacao buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID da avaliação fornecido é inválido.");
        }

        Avaliacao avaliacaoEncontrada = avaliacaoDAO.buscarPorId(id);

        if (avaliacaoEncontrada == null) {
            throw new NoSuchElementException("Avaliação com o ID " + id + " não foi encontrada na base de dados.");
        }

        return avaliacaoEncontrada;
    }

    public List<Avaliacao> buscarPorEvento(Integer idEvento) {

        if (idEvento == null || idEvento <= 0) {
            throw new IllegalArgumentException("O ID do evento fornecido é inválido.");
        }

        List<Avaliacao> resultados = avaliacaoDAO.buscarPorEvento(idEvento);

        return resultados;
    }

    public Avaliacao atualizarAvaliacao(Integer id, Avaliacao dadosAtualizados) {

        Avaliacao avaliacaoExistente = this.buscarPorId(id);

        if (dadosAtualizados.getNota() != null) {
            if (dadosAtualizados.getNota() < 1 || dadosAtualizados.getNota() > 5) {
                throw new IllegalArgumentException("A nova nota da avaliação deve ser um valor entre 1 e 5.");
            }
            avaliacaoExistente.setNota(dadosAtualizados.getNota());
        }

        if (dadosAtualizados.getComentario() != null) {
            avaliacaoExistente.setComentario(dadosAtualizados.getComentario());
        }

        avaliacaoExistente.setDataAvaliacao(LocalDateTime.now());

        boolean sucesso = avaliacaoDAO.atualizar(avaliacaoExistente);

        if (!sucesso) {
            throw new RuntimeException("Falha interna ao tentar atualizar os dados da avaliação.");
        }

        return avaliacaoExistente;
    }

    public void eliminarAvaliacao(Integer id) {

        this.buscarPorId(id);

        boolean sucesso = avaliacaoDAO.deletar(id);

        if (!sucesso) {
            throw new RuntimeException("Falha ao eliminar a avaliação. Tente novamente mais tarde.");
        }
    }

    private boolean mesmaAvaliacao(Avaliacao primeira, Avaliacao segunda) {
        return mesmoId(primeira.getAvaliador(), segunda.getAvaliador())
                && mesmoId(primeira.getAvaliado(), segunda.getAvaliado());
    }

    private boolean mesmoId(Usuario primeiro, Usuario segundo) {
        return primeiro != null
                && segundo != null
                && primeiro.getId() != null
                && primeiro.getId().equals(segundo.getId());
    }
}