package com.disconnect.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.disconnect.domain.Usuario;
import com.disconnect.dto.UsuarioPublicResponseDTO;
import com.disconnect.dto.UsuarioResponseDTO;
import com.disconnect.service.UsuarioService;
import com.disconnect.util.AuthTokenUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import spark.Request;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class UsuarioController {

    private final UsuarioService usuarioService;
    private final Gson gson;

    public UsuarioController(UsuarioService usuarioService, Gson gson) {
        this.usuarioService = usuarioService;
        this.gson = gson;
    }

    public void registerRoutes() {

        post("/api/usuarios", (req, res) -> {
            try {
                Usuario usuarioDaRequisicao = gson.fromJson(req.body(), Usuario.class);

                if (usuarioDaRequisicao == null) {
                    throw new IllegalArgumentException("Corpo da requisição inválido.");
                }

                Usuario usuarioCriado = usuarioService.registarUsuario(usuarioDaRequisicao);
                String token = AuthTokenUtil.gerarToken(usuarioCriado.getId());

                res.status(201);
                return gson.toJson(new UsuarioResponseDTO(usuarioCriado, token));

            } catch (IllegalArgumentException e) {
                res.status(400);
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

        get("/api/usuarios/:id", (req, res) -> {
            try {
                Integer id = Integer.parseInt(req.params(":id"));

                Usuario usuario = usuarioService.buscarPorId(id);

                res.status(200);
                return gson.toJson(new UsuarioPublicResponseDTO(usuario));

            } catch (NumberFormatException e) {
                res.status(400);
                return erro("ID inválido.");

            } catch (IllegalArgumentException e) {
                res.status(400);
                return erro(e.getMessage());

            } catch (NoSuchElementException e) {
                res.status(404);
                return erro(e.getMessage());

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return erro("Erro interno do servidor.");
            }
        });

        get("/api/usuarios", (req, res) -> {
            try {
                String nome = req.queryParams("nome");

                if (nome == null || nome.trim().isEmpty()) {
                    res.status(400);
                    return erro("Forneça o parâmetro ?nome=");
                }

                List<Usuario> usuarios = usuarioService.buscarPorNome(nome);

                List<UsuarioPublicResponseDTO> resposta = usuarios.stream()
                        .map(UsuarioPublicResponseDTO::new)
                        .collect(Collectors.toList());

                res.status(200);
                return gson.toJson(resposta);

            } catch (IllegalArgumentException e) {
                res.status(400);
                return erro(e.getMessage());

            } catch (RuntimeException e) {
                res.status(500);
                return erro("Erro ao buscar usuários.");
            }
        });

        put("/api/usuarios/:id", (req, res) -> {
            try {
                Integer id = Integer.parseInt(req.params(":id"));
                validarUsuarioAutorizado(req, id);

                Usuario dadosAtualizados = gson.fromJson(req.body(), Usuario.class);

                if (dadosAtualizados == null) {
                    throw new IllegalArgumentException("Corpo da requisição inválido.");
                }

                Usuario usuarioSalvo = usuarioService.atualizarUsuario(id, dadosAtualizados);
                String token = AuthTokenUtil.gerarToken(usuarioSalvo.getId());

                res.status(200);
                return gson.toJson(new UsuarioResponseDTO(usuarioSalvo, token));

            } catch (NumberFormatException e) {
                res.status(400);
                return erro("ID inválido.");

            } catch (IllegalArgumentException e) {
                res.status(400);
                return erro(e.getMessage());

            } catch (JsonSyntaxException e) {
                res.status(400);
                return erro("Corpo da requisição inválido.");

            } catch (NoSuchElementException e) {
                res.status(404);
                return erro(e.getMessage());

            } catch (SecurityException e) {
                res.status(403);
                return erro(e.getMessage());

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return erro("Erro interno do servidor.");
            }
        });

        delete("/api/usuarios/:id", (req, res) -> {
            try {
                Integer id = Integer.parseInt(req.params(":id"));
                validarUsuarioAutorizado(req, id);

                usuarioService.eliminarUsuario(id);

                res.status(204);
                return "";

            } catch (NumberFormatException e) {
                res.status(400);
                return erro("ID inválido.");

            } catch (IllegalArgumentException e) {
                res.status(400);
                return erro(e.getMessage());

            } catch (NoSuchElementException e) {
                res.status(404);
                return erro(e.getMessage());

            } catch (SecurityException e) {
                res.status(403);
                return erro(e.getMessage());

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

    private void validarUsuarioAutorizado(Request req, Integer usuarioId) {
        Integer usuarioAutenticadoId = AuthTokenUtil.validarCabecalhoAuthorization(req.headers("Authorization"));
        if (!usuarioId.equals(usuarioAutenticadoId)) {
            throw new SecurityException("Você não tem permissão para alterar este usuário.");
        }
    }

    private static class MensagemErro {

        @SuppressWarnings("unused")
        private final String erro;

        private MensagemErro(String erro) {
            this.erro = erro;
        }
    }
}
