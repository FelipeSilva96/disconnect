package com.disconnect.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.disconnect.domain.Evento;
import com.disconnect.domain.Modalidade;
import com.disconnect.domain.Usuario;
import com.disconnect.domain.enums.FrequenciaEvento;
import com.disconnect.util.ConnectionFactory;

public class EventoDAO {

    public Evento inserir(Evento evento, List<Integer> modalidadeIds) {
        String sql = "INSERT INTO Evento (Nome, Descricao, Data_evento, Localizacao, Latitude, Longitude, Cidade, Frequencia, Url_foto, Dias_semana, Dias_mes, Quant_minima_pessoas, Quant_maxima_pessoas, Nivel_habilidade, Status, Id_organizador) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Integer eventoId;
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, evento.getNome());
                    stmt.setString(2, evento.getDescricao());
                    stmt.setTimestamp(3, Timestamp.valueOf(evento.getDataEvento()));
                    stmt.setString(4, evento.getLocalizacao());
                    setNullableDouble(stmt, 5, evento.getLatitude());
                    setNullableDouble(stmt, 6, evento.getLongitude());
                    stmt.setString(7, evento.getCidade());
                    stmt.setString(8, evento.getFrequencia().name());
                    stmt.setString(9, evento.getUrlFoto());
                    stmt.setString(10, toCsv(evento.getDiasDaSemana()));
                    stmt.setString(11, toCsv(evento.getDiasDoMes()));
                    setNullableInteger(stmt, 12, evento.getQuantMinimaPessoas());
                    setNullableInteger(stmt, 13, evento.getQuantMaximaPessoas());
                    stmt.setString(14, evento.getNivelDeHabilidade());
                    stmt.setString(15, evento.getStatus());
                    stmt.setInt(16, evento.getOrganizador().getId());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Nao foi possivel recuperar o ID do evento criado.");
                        }
                        eventoId = rs.getInt(1);
                    }
                }

                inserirModalidades(conn, eventoId, modalidadeIds);
                conn.commit();
                return buscarPorId(conn, eventoId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao persistir Evento no banco: " + e.getMessage(), e);
        }
    }

    public Evento buscarPorId(Integer id) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarPorId(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Evento por ID: " + e.getMessage(), e);
        }
    }

    public List<Evento> listarTodos() {
        String sql = queryBase() + " ORDER BY e.Data_evento ASC";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Evento> eventos = new ArrayList<>();
            while (rs.next()) {
                eventos.add(mapEvento(rs, conn));
            }
            return eventos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar eventos: " + e.getMessage(), e);
        }
    }

    public List<Evento> listarPorOrganizador(Integer organizadorId) {
        String sql = queryBase() + " WHERE e.Id_organizador = ? ORDER BY e.Data_evento ASC";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, organizadorId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Evento> eventos = new ArrayList<>();
                while (rs.next()) {
                    eventos.add(mapEvento(rs, conn));
                }
                return eventos;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar eventos do organizador: " + e.getMessage(), e);
        }
    }

    public Evento atualizar(Evento evento, List<Integer> modalidadeIds) {
        String sql = "UPDATE Evento SET Nome = ?, Descricao = ?, Data_evento = ?, Localizacao = ?, Latitude = ?, Longitude = ?, Cidade = ?, Frequencia = ?, Url_foto = ?, Dias_semana = ?, Dias_mes = ?, Quant_minima_pessoas = ?, Quant_maxima_pessoas = ?, Nivel_habilidade = ?, Status = ? WHERE Id_evento = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int linhasAfetadas;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, evento.getNome());
                    stmt.setString(2, evento.getDescricao());
                    stmt.setTimestamp(3, Timestamp.valueOf(evento.getDataEvento()));
                    stmt.setString(4, evento.getLocalizacao());
                    setNullableDouble(stmt, 5, evento.getLatitude());
                    setNullableDouble(stmt, 6, evento.getLongitude());
                    stmt.setString(7, evento.getCidade());
                    stmt.setString(8, evento.getFrequencia().name());
                    stmt.setString(9, evento.getUrlFoto());
                    stmt.setString(10, toCsv(evento.getDiasDaSemana()));
                    stmt.setString(11, toCsv(evento.getDiasDoMes()));
                    setNullableInteger(stmt, 12, evento.getQuantMinimaPessoas());
                    setNullableInteger(stmt, 13, evento.getQuantMaximaPessoas());
                    stmt.setString(14, evento.getNivelDeHabilidade());
                    stmt.setString(15, evento.getStatus());
                    stmt.setInt(16, evento.getId());
                    linhasAfetadas = stmt.executeUpdate();
                }

                if (linhasAfetadas == 0) {
                    conn.rollback();
                    return null;
                }

                if (modalidadeIds != null) {
                    deletarModalidades(conn, evento.getId());
                    inserirModalidades(conn, evento.getId(), modalidadeIds);
                }

                conn.commit();
                return buscarPorId(conn, evento.getId());
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar Evento no banco: " + e.getMessage(), e);
        }
    }

    public boolean deletar(Integer id) {
        String sql = "DELETE FROM Evento WHERE Id_evento = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar Evento com ID " + id + ": " + e.getMessage(), e);
        }
    }

    public List<Modalidade> buscarModalidadesPorIds(List<Integer> modalidadeIds) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarModalidadesPorIds(conn, modalidadeIds);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar modalidades do evento: " + e.getMessage(), e);
        }
    }

    public List<Modalidade> listarModalidades() {
        String sql = "SELECT Id_categoria, Nome, Categoria FROM Modalidade ORDER BY Categoria, Nome";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Modalidade> modalidades = new ArrayList<>();
            while (rs.next()) {
                modalidades.add(mapModalidade(rs));
            }
            return modalidades;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar modalidades: " + e.getMessage(), e);
        }
    }

    private void deletarModalidades(Connection conn, Integer eventoId) throws SQLException {
        String sql = "DELETE FROM Modalidade_evento WHERE Id_evento = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventoId);
            stmt.executeUpdate();
        }
    }

    private Evento buscarPorId(Connection conn, Integer id) throws SQLException {
        String sql = queryBase() + " WHERE e.Id_evento = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapEvento(rs, conn);
                }
                return null;
            }
        }
    }

    private void inserirModalidades(Connection conn, Integer eventoId, List<Integer> modalidadeIds)
            throws SQLException {
        String sql = "INSERT INTO Modalidade_evento (Id_evento, Id_modalidade) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer modalidadeId : modalidadeIds) {
                stmt.setInt(1, eventoId);
                stmt.setInt(2, modalidadeId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private Evento mapEvento(ResultSet rs, Connection conn) throws SQLException {
        Evento evento = new Evento();
        evento.setId(rs.getInt("Id_evento"));
        evento.setNome(rs.getString("Nome"));
        evento.setDescricao(rs.getString("Descricao"));

        Timestamp dataEvento = rs.getTimestamp("Data_evento");
        if (dataEvento != null) {
            evento.setDataEvento(dataEvento.toLocalDateTime());
        }

        evento.setLocalizacao(rs.getString("Localizacao"));

        double latitude = rs.getDouble("Latitude");
        if (!rs.wasNull()) {
            evento.setLatitude(latitude);
        }

        double longitude = rs.getDouble("Longitude");
        if (!rs.wasNull()) {
            evento.setLongitude(longitude);
        }

        evento.setCidade(rs.getString("Cidade"));

        String frequencia = rs.getString("Frequencia");
        if (frequencia != null && !frequencia.isBlank()) {
            evento.setFrequencia(FrequenciaEvento.valueOf(frequencia));
        }

        evento.setUrlFoto(rs.getString("Url_foto"));
        evento.setDiasDaSemana(parseStringList(rs.getString("Dias_semana")));
        evento.setDiasDoMes(parseIntegerList(rs.getString("Dias_mes")));

        int quantMinima = rs.getInt("Quant_minima_pessoas");
        if (!rs.wasNull()) {
            evento.setQuantMinimaPessoas(quantMinima);
        }

        int quantMaxima = rs.getInt("Quant_maxima_pessoas");
        if (!rs.wasNull()) {
            evento.setQuantMaximaPessoas(quantMaxima);
        }

        evento.setNivelDeHabilidade(rs.getString("Nivel_habilidade"));
        evento.setStatus(rs.getString("Status"));

        Timestamp dataCriacao = rs.getTimestamp("Data_criacao");
        if (dataCriacao != null) {
            evento.setDataCriacao(dataCriacao.toLocalDateTime());
        }

        Usuario organizador = new Usuario();
        organizador.setId(rs.getInt("Id_usuario"));
        organizador.setNome(rs.getString("Usuario_nome"));
        organizador.setLogin(rs.getString("Login"));

        java.sql.Date dataNascimento = rs.getDate("Data_nascimento");
        if (dataNascimento != null) {
            organizador.setDataNascimento(dataNascimento.toLocalDate());
        }

        organizador.setBiografia(rs.getString("Bio"));
        organizador.setUrlFoto(rs.getString("Usuario_url_foto"));

        Timestamp dataCriacaoUsuario = rs.getTimestamp("Usuario_data_criacao");
        if (dataCriacaoUsuario != null) {
            organizador.setDataCriacao(dataCriacaoUsuario.toLocalDateTime());
        }

        evento.setOrganizador(organizador);
        evento.setModalidades(buscarModalidadesPorEvento(conn, evento.getId()));
        return evento;
    }

    private List<Modalidade> buscarModalidadesPorEvento(Connection conn, Integer eventoId) throws SQLException {
        String sql = "SELECT m.Id_categoria, m.Nome, m.Categoria FROM Modalidade_evento me JOIN Modalidade m ON m.Id_categoria = me.Id_modalidade WHERE me.Id_evento = ? ORDER BY m.Id_categoria";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Modalidade> modalidades = new ArrayList<>();
                while (rs.next()) {
                    modalidades.add(mapModalidade(rs));
                }
                return modalidades;
            }
        }
    }

    private List<Modalidade> buscarModalidadesPorIds(Connection conn, List<Integer> modalidadeIds) throws SQLException {
        if (modalidadeIds == null || modalidadeIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(", ", Collections.nCopies(modalidadeIds.size(), "?"));
        String sql = "SELECT Id_categoria, Nome, Categoria FROM Modalidade WHERE Id_categoria IN (" + placeholders
                + ") ORDER BY Id_categoria";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < modalidadeIds.size(); i++) {
                stmt.setInt(i + 1, modalidadeIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Modalidade> modalidades = new ArrayList<>();
                while (rs.next()) {
                    modalidades.add(mapModalidade(rs));
                }
                return modalidades;
            }
        }
    }

    private Modalidade mapModalidade(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(rs.getInt("Id_categoria"));
        modalidade.setNome(rs.getString("Nome"));
        modalidade.setCategoria(rs.getString("Categoria"));
        return modalidade;
    }

    private void setNullableInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.INTEGER);
            return;
        }
        stmt.setInt(index, value);
    }

    private void setNullableDouble(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.DOUBLE);
            return;
        }
        stmt.setDouble(index, value);
    }

    private String toCsv(List<?> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<String> parseStringList(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Integer> parseIntegerList(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    private String queryBase() {
        return "SELECT e.Id_evento, e.Nome, e.Descricao, e.Data_evento, e.Localizacao, e.Latitude, e.Longitude, e.Cidade, e.Frequencia, e.Url_foto, e.Dias_semana, e.Dias_mes, e.Quant_minima_pessoas, e.Quant_maxima_pessoas, e.Nivel_habilidade, e.Status, e.Data_criacao, u.Id_usuario, u.Nome AS Usuario_nome, u.Login, u.Data_nascimento, u.Bio, u.Url_foto AS Usuario_url_foto, u.Data_criacao AS Usuario_data_criacao FROM Evento e JOIN Usuario u ON u.Id_usuario = e.Id_organizador";
    }
}