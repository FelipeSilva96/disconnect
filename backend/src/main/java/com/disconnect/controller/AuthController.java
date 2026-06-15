package com.disconnect.controller;

import com.google.gson.JsonSyntaxException;
import com.disconnect.domain.Usuario;
import com.disconnect.dto.LoginDTO;
import com.disconnect.dto.UsuarioResponseDTO;
import com.disconnect.service.UsuarioService;
import com.disconnect.util.AuthTokenUtil;
import com.google.gson.Gson;

import static spark.Spark.post;

public class AuthController {

    private final UsuarioService usuarioService;
    private final Gson gson;

    public AuthController(UsuarioService usuarioService, Gson gson) {
        this.usuarioService = usuarioService;
        this.gson = gson;
    }

    public void registerRoutes() {
        post("/api/login", (req, res) -> {
            try {
                LoginDTO loginDTO = gson.fromJson(req.body(), LoginDTO.class);

                if (loginDTO == null) {
                    res.status(400);
                    return erro("Corpo da requisição inválido.");
                }

                if (loginDTO.getLogin() == null || loginDTO.getLogin().trim().isEmpty()
                        || loginDTO.getSenha() == null || loginDTO.getSenha().trim().isEmpty()) {
                    res.status(400);
                    return erro("Login e senha são obrigatórios.");
                }

                Usuario usuarioLogado = usuarioService.autenticar(
                        loginDTO.getLogin(),
                        loginDTO.getSenha());
                String token = AuthTokenUtil.gerarToken(usuarioLogado.getId());

                res.status(200);
                return gson.toJson(new UsuarioResponseDTO(usuarioLogado, token));

            } catch (IllegalArgumentException e) {
                res.status(401);
                return erro(e.getMessage());

            } catch (JsonSyntaxException e) {
                res.status(400);
                return erro("Corpo da requisição inválido.");

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return erro("Erro interno do servidor.");
            }
        });
    }

    private String erro(String mensagem) {
        return gson.toJson(new MensagemErro(mensagem));
    }

    private static class MensagemErro {

        @SuppressWarnings("unused")
        private final String erro;

        private MensagemErro(String erro) {
            this.erro = erro;
        }
    }
}
