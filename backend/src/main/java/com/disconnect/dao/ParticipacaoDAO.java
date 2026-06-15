package com.disconnect.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.disconnect.domain.Evento;
import com.disconnect.domain.Participacao;
import com.disconnect.domain.Usuario;
import com.disconnect.domain.enums.StatusParticipacao;
import com.disconnect.util.ConnectionFactory;

public class ParticipacaoDAO {

    private final EventoDAO eventoDAO;
    private final UsuarioDAO usuarioDAO;

    public ParticipacaoDAO() {
        this.eventoDAO = new EventoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public Participacao inserir(Participacao participacao) {
        String sql = "INSERT INTO Participacao (Id_evento, Id_usuario, Status, Data_solicitacao, Mensagem_solicitacao) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, participacao.getEvento().getId());
            stmt.setInt(2, participacao.getSolicitante().getId());
            stmt.setString(3, participacao.getStatus().name());
            setNullableTimestamp(stmt, 4, participacao.getDataSolicitacao());
            stmt.setString(5, participacao.getMensagemSolicitacao());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Nao foi possivel criar participacao.");
            }

            return buscarPorUsuarioEEvento(participacao.getSolicitante().getId(), participacao.getEvento().getId());
        } catch (SQLException e) {
            throw traduzirErroPersistencia(e);
        }
    }

    public List<Participacao> listarPorEvento(Integer idEvento) {
        String sql = "SELECT * FROM Participacao WHERE Id_evento = ? ORDER BY Data_solicitacao DESC";
        return listarPorParametro(sql, idEvento);

    }

    public List<Participacao> listarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM Participacao WHERE Id_usuario = ? ORDER BY Data_solicitacao DESC";
        return listarPorParametro(sql, idUsuario);

    }

    private List<Participacao> listarPorParametro(String sql, Integer parametro) {
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Participacao> participacoes = new ArrayList<>();
                while (rs.next()) {
                    participacoes.add(mapParticipacao(rs));
                }
                return participacoes;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar Participacoes: " + e.getMessage(), e);
        }
    }

    private Participacao mapParticipacao(ResultSet rs) throws SQLException {
        Participacao participacao = new Participacao();

        String status = rs.getString("Status");
        if (status != null && !status.isBlank()) {
            participacao.setStatus(StatusParticipacao.valueOf(status));
        }

        Timestamp dataSolicitacao = rs.getTimestamp("Data_solicitacao");
        if (dataSolicitacao != null) {
            participacao.setDataSolicitacao(dataSolicitacao.toLocalDateTime());
        }

        participacao.setMensagemSolicitacao(rs.getString("Mensagem_solicitacao"));
        participacao.setMensagemResposta(rs.getString("Mensagem_resposta"));

        Timestamp dataResposta = rs.getTimestamp("Data_resposta");
        if (dataResposta != null) {
            participacao.setDataResposta(dataResposta.toLocalDateTime());
        }

        Usuario solicitante = usuarioDAO.buscarPorId(rs.getInt("Id_usuario"));
        Evento evento = eventoDAO.buscarPorId(rs.getInt("Id_evento"));
        participacao.setSolicitante(solicitante);
        participacao.setEvento(evento);
        return participacao;
    }

    public boolean deletar(Integer id_evento, Integer id_usuario) {
        String sql = "DELETE FROM Participacao WHERE Id_evento = ? AND Id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id_evento);
            stmt.setInt(2, id_usuario);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar Participacao com ID " + id_evento + " e Usuario " + id_usuario
                    + ": " + e.getMessage(), e);
        }
    }

    public Participacao atualizar(Participacao participacao) {
        String sql = "UPDATE Participacao SET Mensagem_solicitacao = ? WHERE Id_evento = ? AND Id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participacao.getMensagemSolicitacao());
            stmt.setInt(2, participacao.getEvento().getId());
            stmt.setInt(3, participacao.getSolicitante().getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Nenhuma Participacao encontrada para atualizar com ID "
                        + participacao.getEvento().getId() + " e Usuario " + participacao.getSolicitante().getId());
            }
            return buscarPorUsuarioEEvento(participacao.getSolicitante().getId(), participacao.getEvento().getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar Participacao com ID " + participacao.getEvento().getId()
                    + " e Usuario " + participacao.getSolicitante().getId() + ": " + e.getMessage(), e);
        }
    }

    public Participacao responderSolicitacao(Participacao participacao) {
        String sql = "UPDATE Participacao SET Status = ?, Mensagem_resposta = ?, Data_resposta = ? WHERE Id_evento = ? AND Id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participacao.getStatus().name());
            stmt.setString(2, participacao.getMensagemResposta());
            setNullableTimestamp(stmt, 3, participacao.getDataResposta());
            stmt.setInt(4, participacao.getEvento().getId());
            stmt.setInt(5, participacao.getSolicitante().getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Nenhuma Participacao encontrada para responder com ID "
                        + participacao.getEvento().getId() + " e Usuario " + participacao.getSolicitante().getId());
            }
            return buscarPorUsuarioEEvento(participacao.getSolicitante().getId(), participacao.getEvento().getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao responder Participacao com ID " + participacao.getEvento().getId()
                    + " e Usuario " + participacao.getSolicitante().getId() + ": " + e.getMessage(), e);
        }
    }

    public Participacao buscarPorUsuarioEEvento(Integer id_usuario, Integer id_evento) {
        String sql = "SELECT * FROM Participacao WHERE Id_usuario = ? AND Id_evento = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id_usuario);
            stmt.setInt(2, id_evento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapParticipacao(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Participacao por Usuario " + id_usuario + " e Evento "
                    + id_evento + ": " + e.getMessage(), e);
        }
    }

    private void setNullableTimestamp(PreparedStatement stmt, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.TIMESTAMP);
            return;
        }
        stmt.setTimestamp(index, Timestamp.valueOf(value));
    }

    private RuntimeException traduzirErroPersistencia(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new IllegalStateException("Usuario ja solicitou participacao neste evento.");
        }

        if ("23503".equals(e.getSQLState())) {
            return new IllegalArgumentException("Usuario ou Evento nao encontrado.");
        }

        return new RuntimeException("Erro ao persistir Participacao no banco: " + e.getMessage(), e);
    }
}
