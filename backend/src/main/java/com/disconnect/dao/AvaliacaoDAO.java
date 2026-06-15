package com.disconnect.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.disconnect.domain.Avaliacao;
import com.disconnect.domain.Usuario;
import com.disconnect.domain.Evento;
import com.disconnect.util.ConnectionFactory;

public class AvaliacaoDAO {

    public Avaliacao inserir(Avaliacao avaliacao) {
        String sql = "INSERT INTO Avaliacao (Id_avaliador, Id_avaliado, Id_evento, nota, comentario, Data_avaliacao) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, avaliacao.getAvaliador().getId());
            stmt.setInt(2, avaliacao.getAvaliado().getId());
            stmt.setInt(3, avaliacao.getEvento().getId());
            stmt.setInt(4, avaliacao.getNota());

            if (avaliacao.getComentario() != null) {
                stmt.setString(5, avaliacao.getComentario());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(avaliacao.getDataAvaliacao()));

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        avaliacao.setId(rs.getInt(1));
                    }
                }
            }
            return avaliacao;
        } catch (SQLException e) {

            throw translateInsertException(e);
        }
    }

    private RuntimeException translateInsertException(SQLException e) {

        if ("23505".equals(e.getSQLState())) {
            return new IllegalArgumentException("Este usuário já avaliou este evento.");
        }

        if ("23503".equals(e.getSQLState())) {
            return new IllegalArgumentException("Avaliador, avaliado ou evento não encontrado no sistema.");
        }

        return new RuntimeException("Erro ao persistir Avaliacao no banco: " + e.getMessage(), e);
    }

    public Avaliacao buscarPorId(Integer id) {
        String sql = "SELECT * FROM Avaliacao WHERE Id_avaliacao = ?";
        Avaliacao avaliacao = null;

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    avaliacao = new Avaliacao();
                    avaliacao.setId(rs.getInt("Id_avaliacao"));
                    avaliacao.setNota(rs.getInt("nota"));
                    avaliacao.setComentario(rs.getString("comentario"));

                    java.sql.Timestamp dataAvaliacao = rs.getTimestamp("Data_avaliacao");
                    if (dataAvaliacao != null) {
                        avaliacao.setDataAvaliacao(dataAvaliacao.toLocalDateTime());
                    }

                    Usuario avaliador = new Usuario();
                    avaliador.setId(rs.getInt("Id_avaliador"));
                    avaliacao.setAvaliador(avaliador);

                    int avaliadoId = rs.getInt("Id_avaliado");
                    if (!rs.wasNull()) {
                        Usuario avaliado = new Usuario();
                        avaliado.setId(avaliadoId);
                        avaliacao.setAvaliado(avaliado);
                    }

                    Evento evento = new Evento();
                    evento.setId(rs.getInt("Id_evento"));
                    avaliacao.setEvento(evento);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Avaliacao por ID: " + e.getMessage(), e);
        }

        return avaliacao;
    }

    public List<Avaliacao> buscarPorEvento(Integer idEvento) {
        String sql = "SELECT * FROM Avaliacao WHERE Id_evento = ?";
        List<Avaliacao> listaResultados = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEvento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Avaliacao a = new Avaliacao();
                    a.setId(rs.getInt("Id_avaliacao"));
                    a.setNota(rs.getInt("nota"));
                    a.setComentario(rs.getString("comentario"));

                    java.sql.Timestamp dataAvaliacao = rs.getTimestamp("Data_avaliacao");
                    if (dataAvaliacao != null) {
                        a.setDataAvaliacao(dataAvaliacao.toLocalDateTime());
                    }

                    Usuario avaliador = new Usuario();
                    avaliador.setId(rs.getInt("Id_avaliador"));
                    a.setAvaliador(avaliador);

                    int avaliadoId = rs.getInt("Id_avaliado");
                    if (!rs.wasNull()) {
                        Usuario avaliado = new Usuario();
                        avaliado.setId(avaliadoId);
                        a.setAvaliado(avaliado);
                    }

                    Evento evento = new Evento();
                    evento.setId(rs.getInt("Id_evento"));
                    a.setEvento(evento);

                    listaResultados.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar avaliações do evento com ID " + idEvento, e);
        }
        return listaResultados;
    }

    public boolean atualizar(Avaliacao avaliacao) {

        String sql = "UPDATE Avaliacao SET nota = ?, comentario = ?, Data_avaliacao = ? WHERE Id_avaliacao = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, avaliacao.getNota());

            if (avaliacao.getComentario() != null) {
                stmt.setString(2, avaliacao.getComentario());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(avaliacao.getDataAvaliacao()));

            stmt.setInt(4, avaliacao.getId());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar a Avaliacao com ID " + avaliacao.getId() + ": " + e.getMessage(), e);
        }
    }

    public boolean deletar(Integer id) {
        String sql = "DELETE FROM Avaliacao WHERE Id_avaliacao = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar a Avaliacao com ID " + id + ": " + e.getMessage(), e);
        }
    }
}