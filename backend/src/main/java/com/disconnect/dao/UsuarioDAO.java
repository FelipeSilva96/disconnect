package com.disconnect.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.disconnect.domain.Usuario;
import com.disconnect.util.ConnectionFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class UsuarioDAO {

    private static final Gson GSON = new Gson();
    private static final Type LISTA_STRINGS = new TypeToken<List<String>>() {
    }.getType();
    private static final Type MAPA_STRINGS = new TypeToken<Map<String, String>>() {
    }.getType();

    // Essa função inserir é o 'C' de CREATE no CRUD. Ela insere um novo usuário e
    // recupera o ID (SERIAL) gerado.
    public Usuario inserir(Usuario usuario) {

        // A query SQL usa parâmetros '?' em vez de concatenação de strings. Se
        // estivessemos usando ORM não teria essa parte verbosa
        String sql = "INSERT INTO Usuario (Nome, Email, Login, Senha, Data_nascimento, Bio, Url_foto, Localizacao, Hobbies, Nivel_experiencia, Is_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Uso do "try-with-resources". A sintaxe de colocar a inicialização dentro do
        // () do try garante que o Java vai chamar silenciosamente conn.close() e
        // stmt.close() ao final do bloco, mesmo que ocorra um erro, pra não vazar
        // memoria.
        try (Connection conn = ConnectionFactory.getConnection(); // Informamos ao driver que queremos o ID de volta
                                                                  // após o insert
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getLogin());
            stmt.setString(4, usuario.getSenha());

            if (usuario.getDataNascimento() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(usuario.getDataNascimento()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setString(6, usuario.getBiografia());
            stmt.setString(7, usuario.getUrlFoto()); // Este campo precisará de conversão se usar CamelCase no Java
            stmt.setString(8, usuario.getLocalizacao());
            stmt.setString(9, toJson(usuario.getHobbies()));
            stmt.setString(10, toJson(usuario.getNivelExperiencia()));
            stmt.setBoolean(11, usuario.getIsAdmin() != null ? usuario.getIsAdmin() : false);

            // Dispara o comando para o banco. Retorna o número de linhas afetadas
            // (esperado: 1)
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                // Recupera a tabela virtual retornada contendo a chave primária gerada (o
                // SERIAL)
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Injeta o ID criado pelo PostgreSQL de volta no objeto Java
                        usuario.setId(rs.getInt(1));
                    }
                }
            }
            return usuario; // Retorna um objeto de Java mesmo, com um ID SERIAL gerado automaticamente pelo
                            // postgre, por isso não podia colocar la la no SQL chave privada como int,
                            // senão teria que tratar do ID aqui

        } catch (SQLException e) {
            throw translateInsertException(e);
        }
    }

    private RuntimeException translateInsertException(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            String message = e.getMessage() != null ? e.getMessage() : "";

            if (message.contains("usuario_email_key")) {
                return new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
            }

            if (message.contains("usuario_login_key")) {
                return new IllegalArgumentException("Já existe um usuário cadastrado com este login.");
            }

            return new IllegalArgumentException("Já existe um usuário cadastrado com estes dados.");
        }

        return new RuntimeException("Erro ao inserir Usuário no banco: " + e.getMessage(), e);
    }

    // Essa função de buscar é o 'R' DO CRUD, essa busca especificamente por ID.
    // Podem ser implementadas outras funções semelhantes pra buscar por qualquer
    // coisa (nome por ex).
    // mais parte verbosa em Java puro. (pelo menos dá pro Bruno escrever mais SQL)
    public Usuario buscarPorId(Integer id) {
        String sql = selectUsuarioComAvaliacoes() + " WHERE u.Id_usuario = ? GROUP BY u.Id_usuario";
        Usuario usuario = null;

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Usuario por ID: " + e.getMessage(), e);
        }

        return usuario; // Retorna nulo se o ID não for encontrado
    }

    // Continua sendo R do CRUDM, mas o retorno agora é uma List<Usuario>, pois
    // podem existir 5 "Brunos" no sistema.
    public List<Usuario> buscarPorNome(String parteDoNome) {
        // O ILIKE ignora letras maiúsculas/minúsculas.
        String sql = selectUsuarioComAvaliacoes() + " WHERE u.Nome ILIKE ? GROUP BY u.Id_usuario ORDER BY u.Nome";
        List<Usuario> listaResultados = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Se a pessoa digitou "lip", a gente injeta "%lip%" no banco. Pra facilitar ne
            // O banco retornará "Felipe", "Lipe", "Alípio", etc.
            stmt.setString(1, "%" + parteDoNome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listaResultados.add(mapUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuários pelo nome", e);
        }
        return listaResultados;
    }

    // Busca um usuário especificamente pelo Login exato
    public Usuario buscarPorLogin(String login) {
        String sql = selectUsuarioComAvaliacoes() + " WHERE u.Login = ? GROUP BY u.Id_usuario";
        Usuario usuario = null;

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Usuario por Login", e);
        }
        return usuario; // Retorna null se o login não existir
    }

    public Usuario buscarPorEmail(String email) {
        String sql = selectUsuarioComAvaliacoes() + " WHERE u.Email = ? GROUP BY u.Id_usuario";
        Usuario usuario = null;

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Usuario por Email", e);
        }
        return usuario;
    }

    // Aqui é o 'U' do CRUD. Atualiza os dados de um usuário já existente. Sem
    // segredo.
    public boolean atualizar(Usuario usuario) {
        // A cláusula WHERE Id_usuario = ? é VITAL. Sem ela, você daria um update na
        // tabela inteira.
        String sql = "UPDATE Usuario SET Nome = ?, Login = ?, Email = ?, Senha = ?, Data_nascimento = ?, Bio = ?, Url_foto = ?, Localizacao = ?, Hobbies = ?, Nivel_experiencia = ?, Is_admin = ? WHERE Id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());

            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenha());

            if (usuario.getDataNascimento() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(usuario.getDataNascimento()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setString(6, usuario.getBiografia()); // Lembra de checar o nome deste getter nas classes que forem
                                                       // mexer. Porque quando eu criei as classes e fiz os getters e
                                                       // setters, não tinha feito atualização das tabelas ainda não.

            stmt.setString(7, usuario.getUrlFoto());
            stmt.setString(8, usuario.getLocalizacao());
            stmt.setString(9, toJson(usuario.getHobbies()));
            stmt.setString(10, toJson(usuario.getNivelExperiencia()));
            stmt.setBoolean(11, usuario.getIsAdmin() != null ? usuario.getIsAdmin() : false);

            // O último parâmetro é o ID que vai na cláusula WHERE lá no final da query
            stmt.setInt(12, usuario.getId());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw translateUpdateException(usuario.getId(), e);
        }
    }

    private RuntimeException translateUpdateException(Integer id, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            String message = e.getMessage() != null ? e.getMessage() : "";

            if (message.contains("usuario_email_key")) {
                return new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
            }

            if (message.contains("usuario_login_key")) {
                return new IllegalArgumentException("Já existe um usuário cadastrado com este login.");
            }

            return new IllegalArgumentException("Já existe um usuário cadastrado com estes dados.");
        }

        return new RuntimeException("Erro ao atualizar o Usuario com ID " + id + ": " + e.getMessage(), e);
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("Id_usuario"));
        usuario.setNome(rs.getString("Nome"));
        usuario.setEmail(rs.getString("Email"));
        usuario.setSenha(rs.getString("Senha"));

        java.sql.Date dataNascimento = rs.getDate("Data_nascimento");
        if (dataNascimento != null) {
            usuario.setDataNascimento(dataNascimento.toLocalDate());
        }

        usuario.setBiografia(rs.getString("Bio"));
        usuario.setUrlFoto(rs.getString("Url_foto"));
        usuario.setLocalizacao(rs.getString("Localizacao"));
        usuario.setHobbies(parseListaStrings(rs.getString("Hobbies")));
        usuario.setNivelExperiencia(parseMapaStrings(rs.getString("Nivel_experiencia")));
        double avaliacaoMedia = rs.getDouble("Avaliacao_media");
        usuario.setAvaliacaoMedia(rs.wasNull() ? null : avaliacaoMedia);
        usuario.setTotalAvaliacoes(rs.getInt("Total_avaliacoes"));
        usuario.setIsAdmin(rs.getBoolean("Is_admin"));
        usuario.setLogin(rs.getString("Login"));

        java.sql.Timestamp dataCriacao = rs.getTimestamp("Data_criacao");
        if (dataCriacao != null) {
            usuario.setDataCriacao(dataCriacao.toLocalDateTime());
        }

        return usuario;
    }

    private String selectUsuarioComAvaliacoes() {
        return "SELECT u.*, AVG(a.Nota) AS Avaliacao_media, COUNT(a.Id_avaliacao) AS Total_avaliacoes FROM Usuario u LEFT JOIN Avaliacao a ON a.Id_avaliado = u.Id_usuario";
    }

    private String toJson(Object valor) {
        return valor != null ? GSON.toJson(valor) : null;
    }

    private List<String> parseListaStrings(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            List<String> valores = GSON.fromJson(json, LISTA_STRINGS);
            return valores != null ? valores : new ArrayList<>();
        } catch (JsonSyntaxException e) {
            return new ArrayList<>();
        }
    }

    private Map<String, String> parseMapaStrings(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        try {
            Map<String, String> valores = GSON.fromJson(json, MAPA_STRINGS);
            return valores != null ? valores : new HashMap<>();
        } catch (JsonSyntaxException e) {
            return new HashMap<>();
        }
    }

    // O 'D' do CRUD. Exclui um usuário do sistema permanentemente.
    public boolean deletar(Integer id) {
        String sql = "DELETE FROM Usuario WHERE Id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhasAfetadas = stmt.executeUpdate();

            // Graças à arquitetura que o Bruno montou nas Migrations (ON DELETE CASCADE),
            // deletar o usuário aqui vai limpar automaticamente todos os eventos e
            // avaliações que ele fez.
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar o Usuario com ID " + id + ": " + e.getMessage(), e);
        }
    }
}
