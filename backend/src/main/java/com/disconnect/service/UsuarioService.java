package com.disconnect.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import com.disconnect.dao.UsuarioDAO;
import com.disconnect.domain.Usuario;
import com.disconnect.util.PasswordUtil;

//  Camada de Lógica de Negócio para o domínio de usuarios. Responsável por aplicar validações, regras de segurança e fazer as chamadas ao DAO.
public class UsuarioService {

    // Fluxo -> Front -> Controller -> Service -> DAO.
    // As camadas só olham "pra frente". Então o service chama o DAO e o DAO vai
    // retornar algo pro service.
    // Ah e o Service não sabe o que é JDBC ou PostgreSQL, ele apenas confia que o
    // DAO vai fazer o que tem que fazer.
    // Quando chega aqui, não tem SQL mais, é só objeto Java.
    private UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // 'C' do CRUD, só que voltado as regras do que pode ou não fazer ao registrar
    public Usuario registarUsuario(Usuario usuario) {
        // 1. Validações de Domínio (Fail Fast) ou seja, se algo não passar, já da erro
        // rapido

        String nomeNormalizado = usuario.getNome() != null ? usuario.getNome().trim() : null;
        String emailNormalizado = usuario.getEmail() != null ? usuario.getEmail().trim() : null;
        String loginNormalizado = usuario.getLogin() != null ? usuario.getLogin().trim() : null;

        if (nomeNormalizado == null || nomeNormalizado.isEmpty()) {
            throw new IllegalArgumentException("O nome do utilizador é obrigatório e não pode estar vazio.");
        }

        if (emailNormalizado == null || emailNormalizado.isEmpty() || !emailNormalizado.contains("@")) {
            throw new IllegalArgumentException("O formato do e-mail fornecido é inválido.");
        }

        if (loginNormalizado == null || loginNormalizado.isEmpty()) {
            throw new IllegalArgumentException("O login do utilizador é obrigatório e não pode estar vazio.");
        }

        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new IllegalArgumentException(
                    "Por razões de segurança, a palavra-passe deve ter no mínimo 6 caracteres.");
        }

        validarDataNascimento(usuario.getDataNascimento());

        if (usuario.getDataNascimento() != null && usuario.getIdade() < 12) {
            throw new IllegalArgumentException(
                    "O utilizador deve ter pelo menos 12 anos para se registar na plataforma.");
        }

        usuario.setNome(nomeNormalizado);
        usuario.setEmail(emailNormalizado);
        usuario.setLogin(loginNormalizado);
        usuario.setIsAdmin(false);

        // 2. Enviar para a camada de persistência
        // Se todas as regras de negócio passarem, chamamos o DAO.
        // mas antes criptografa ne
        String senhaHash = PasswordUtil.gerarHash(usuario.getSenha());
        usuario.setSenha(senhaHash);

        return usuarioDAO.inserir(usuario);

    }

    // R do CRUD...
    public Usuario buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID do utilizador fornecido é inválido.");
        }

        Usuario usuarioEncontrado = usuarioDAO.buscarPorId(id);

        if (usuarioEncontrado == null) {

            throw new NoSuchElementException("Utilizador com o ID " + id + " não foi encontrado na base de dados.");
        }

        return usuarioEncontrado;
    }

    // READ: Buscar usuarios por Nome (Para o motor de pesquisa do Front)
    public List<Usuario> buscarPorNome(String parteDoNome) {
        // Validação Fail Fast: Bloqueia buscas nulas ou com apenas espaços em branco
        if (parteDoNome == null || parteDoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("O termo de busca não pode estar vazio.");
        }

        // Confia no DAO para executar o ILIKE no banco de dados. - Confia pow =D
        List<Usuario> resultados = usuarioDAO.buscarPorNome(parteDoNome.trim());

        // Diferente da busca por ID (que dá erro se não achar), na busca por nome, aqui
        // pode retornar uma lista vazia se ninguém se chamar "Xyz", dai o Front vai
        // receber a lista vazia e desenhar a tela "Nenhum usuário encontrado", ou algo
        // assim.
        return resultados;
    }

    // U do CRUD...
    public Usuario atualizarUsuario(Integer id, Usuario dadosAtualizados) {
        // Garantir que o usuario realmente existe antes de tentar atualizar
        Usuario usuarioExistente = this.buscarPorId(id);

        // Aplicar apenas os campos permitidos
        if (dadosAtualizados.getNome() != null && !dadosAtualizados.getNome().trim().isEmpty()) {
            usuarioExistente.setNome(dadosAtualizados.getNome().trim());
        }

        if (dadosAtualizados.getEmail() != null) {
            if (!dadosAtualizados.getEmail().contains("@")) {
                throw new IllegalArgumentException("O formato do e-mail fornecido é inválido.");
            }
            usuarioExistente.setEmail(dadosAtualizados.getEmail().trim());
        }

        if (dadosAtualizados.getDataNascimento() != null) {
            validarDataNascimento(dadosAtualizados.getDataNascimento());
            if (dadosAtualizados.getIdade() < 12) {
                throw new IllegalArgumentException("O utilizador deve ter pelo menos 12 anos para usar a plataforma.");
            }
            usuarioExistente.setDataNascimento(dadosAtualizados.getDataNascimento());
        }

        if (dadosAtualizados.getBiografia() != null) {
            usuarioExistente.setBiografia(dadosAtualizados.getBiografia());
        }

        if (dadosAtualizados.getUrlFoto() != null) {
            usuarioExistente.setUrlFoto(dadosAtualizados.getUrlFoto());
        }

        if (dadosAtualizados.getLocalizacao() != null) {
            usuarioExistente.setLocalizacao(dadosAtualizados.getLocalizacao().trim());
        }

        if (dadosAtualizados.getHobbies() != null) {
            usuarioExistente.setHobbies(dadosAtualizados.getHobbies());
        }

        if (dadosAtualizados.getNivelExperiencia() != null) {
            usuarioExistente.setNivelExperiencia(dadosAtualizados.getNivelExperiencia());
        }

        // Gravar no banco de dados
        boolean sucesso = usuarioDAO.atualizar(usuarioExistente);

        if (!sucesso) {
            throw new RuntimeException("Falha interna ao tentar atualizar os dados do utilizador.");
        }

        return usuarioExistente;
    }

    // D do CRUD...
    public void eliminarUsuario(Integer id) {
        // A própria função buscarPorId já valida se o ID existe e lança exceção se não
        // existir
        this.buscarPorId(id);

        boolean sucesso = usuarioDAO.deletar(id);

        if (!sucesso) {
            throw new RuntimeException(
                    "Falha ao eliminar a conta do utilizador. Pode estar bloqueada por restrições da base de dados.");
        }
    }

    // Valida as credenciais de acesso do usuário.
    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null || login.trim().isEmpty() || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Login e senha são obrigatórios.");
        }

        String credencial = login.trim();
        Usuario usuarioBanco = usuarioDAO.buscarPorLogin(credencial);

        if (usuarioBanco == null && credencial.contains("@")) {
            usuarioBanco = usuarioDAO.buscarPorEmail(credencial);
        }

        if (usuarioBanco == null || !PasswordUtil.verificarSenha(senha, usuarioBanco.getSenha())) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        return usuarioBanco; // Se o login deu bom
    }

    private void validarDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento != null && dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
        }
    }
}
