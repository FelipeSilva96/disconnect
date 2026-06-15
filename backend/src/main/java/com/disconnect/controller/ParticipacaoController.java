package com.disconnect.controller;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.disconnect.dto.ParticipacaoRequestDTO;
import com.disconnect.dto.ParticipacaoRespostaDTO;
import com.disconnect.dto.ParticipacaoResponseDTO;
import com.disconnect.domain.enums.StatusParticipacao;
import com.disconnect.service.ParticipacaoService;
import com.google.gson.Gson;

import spark.Request;
import spark.Response;

public class ParticipacaoController {

    private final ParticipacaoService participacaoService;
    private final Gson gson;

    public ParticipacaoController() {
        this(new ParticipacaoService(), new Gson());
    }

    public ParticipacaoController(ParticipacaoService participacaoService, Gson gson) {
        this.participacaoService = participacaoService;
        this.gson = gson;
    }

    public void registerRoutes() {
        post("/api/participacoes", this::criarParticipacao);
        post("/api/participacao", this::criarParticipacao);
        get("/api/participacoes", this::listarParticipacoes);
        put("/api/participacoes/:eventoId/:usuarioId", this::atualizarMensagem);
        put("/api/participacoes/:eventoId/:usuarioId/resposta", this::responderSolicitacao);
        delete("/api/participacoes/:eventoId/:usuarioId", this::deletarParticipacao);
    }

    private Object criarParticipacao(Request request, Response response) {
        response.type("application/json");

        try {
            ParticipacaoRequestDTO participacaoRequest = gson.fromJson(request.body(), ParticipacaoRequestDTO.class);

            if (participacaoRequest == null) {
                throw new IllegalArgumentException("Corpo da requisicao invalido.");
            }

            ParticipacaoResponseDTO participacaoResponse = new ParticipacaoResponseDTO(
                    participacaoService.solicitarParticipacao(participacaoRequest.getId_solicitante(),
                            participacaoRequest.getId_evento(), participacaoRequest.getMensagem_solicitacao()));

            response.status(201);
            return gson.toJson(participacaoResponse);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.status(400);
            return errorJson(e.getMessage());
        } catch (NoSuchElementException e) {
            response.status(404);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return errorJson("Erro interno do servidor.");
        }
    }

    private Object listarParticipacoes(Request request, Response response) {
        response.type("application/json");

        try {
            String eventoIdParam = request.queryParams("eventoId");
            String usuarioIdParam = request.queryParams("usuarioId");
            List<ParticipacaoResponseDTO> participacoes;

            if (eventoIdParam != null && !eventoIdParam.isBlank()) {
                Integer eventoId = Integer.parseInt(eventoIdParam);
                participacoes = participacaoService.listarPorEvento(eventoId).stream()
                        .map(ParticipacaoResponseDTO::new)
                        .collect(Collectors.toList());
            } else if (usuarioIdParam != null && !usuarioIdParam.isBlank()) {
                Integer usuarioId = Integer.parseInt(usuarioIdParam);
                participacoes = participacaoService.listarPorUsuario(usuarioId).stream()
                        .map(ParticipacaoResponseDTO::new)
                        .collect(Collectors.toList());
            } else {
                throw new IllegalArgumentException("Forneca um parametro ?eventoId= ou ?usuarioId=.");
            }

            response.status(200);
            return gson.toJson(participacoes);
        } catch (NumberFormatException e) {
            response.status(400);
            return errorJson("Os parametros de busca devem ser numeros validos.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.status(400);
            return errorJson(e.getMessage());
        } catch (NoSuchElementException e) {
            response.status(404);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return errorJson("Erro interno do servidor.");
        }
    }

    private Object atualizarMensagem(Request request, Response response) {
        response.type("application/json");

        try {
            Integer eventoId = Integer.parseInt(request.params(":eventoId"));
            Integer usuarioId = Integer.parseInt(request.params(":usuarioId"));
            ParticipacaoRequestDTO participacaoRequest = gson.fromJson(request.body(), ParticipacaoRequestDTO.class);

            if (participacaoRequest == null) {
                throw new IllegalArgumentException("Corpo da requisicao invalido.");
            }

            ParticipacaoResponseDTO participacaoResponse = new ParticipacaoResponseDTO(
                    participacaoService.atualizarMensagem(eventoId, usuarioId,
                            participacaoRequest.getMensagem_solicitacao()));

            response.status(200);
            return gson.toJson(participacaoResponse);
        } catch (NumberFormatException e) {
            response.status(400);
            return errorJson("IDs invalidos.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.status(400);
            return errorJson(e.getMessage());
        } catch (NoSuchElementException e) {
            response.status(404);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return errorJson("Erro interno do servidor.");
        }
    }

    private Object responderSolicitacao(Request request, Response response) {
        response.type("application/json");

        try {
            Integer eventoId = Integer.parseInt(request.params(":eventoId"));
            Integer usuarioId = Integer.parseInt(request.params(":usuarioId"));
            ParticipacaoRespostaDTO respostaRequest = gson.fromJson(request.body(), ParticipacaoRespostaDTO.class);

            if (respostaRequest == null) {
                throw new IllegalArgumentException("Corpo da requisicao invalido.");
            }

            StatusParticipacao status = parseStatus(respostaRequest.getStatus());
            ParticipacaoResponseDTO participacaoResponse = new ParticipacaoResponseDTO(
                    participacaoService.responderSolicitacao(eventoId, usuarioId, status,
                            respostaRequest.getMensagem_resposta()));

            response.status(200);
            return gson.toJson(participacaoResponse);
        } catch (NumberFormatException e) {
            response.status(400);
            return errorJson("IDs invalidos.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.status(400);
            return errorJson(e.getMessage());
        } catch (NoSuchElementException e) {
            response.status(404);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return errorJson("Erro interno do servidor.");
        }
    }

    private Object deletarParticipacao(Request request, Response response) {
        response.type("application/json");

        try {
            Integer eventoId = Integer.parseInt(request.params(":eventoId"));
            Integer usuarioId = Integer.parseInt(request.params(":usuarioId"));
            participacaoService.deletar(eventoId, usuarioId);

            response.status(204);
            return "";
        } catch (NumberFormatException e) {
            response.status(400);
            return errorJson("IDs invalidos.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.status(400);
            return errorJson(e.getMessage());
        } catch (NoSuchElementException e) {
            response.status(404);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return errorJson("Erro interno do servidor.");
        }
    }

    private StatusParticipacao parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("O status da resposta e obrigatorio.");
        }

        return StatusParticipacao.valueOf(status.trim().toUpperCase());
    }

    private String errorJson(String message) {
        return gson.toJson(Map.of("erro", message != null ? message : "Erro desconhecido."));
    }
}
