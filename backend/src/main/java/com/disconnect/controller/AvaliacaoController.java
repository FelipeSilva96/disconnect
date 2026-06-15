package com.disconnect.controller;


import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.disconnect.domain.Avaliacao;
import com.disconnect.dto.AvaliacaoResponseDTO;
import com.disconnect.service.AvaliacaoService;
import com.google.gson.Gson;

public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final Gson gson;

    public AvaliacaoController(AvaliacaoService avaliacaoService, Gson gson) {
        this.avaliacaoService = avaliacaoService;
        this.gson = gson;
    }

    public void registerRoutes() {
        // C - CREATE: Cria uma nova avaliação
        post("/api/avaliacoes", (request, response) -> {
            response.type("application/json");

            try {
                Avaliacao avaliacaoDaRequisicao = gson.fromJson(request.body(), Avaliacao.class);
                if (avaliacaoDaRequisicao == null) {
                    throw new IllegalArgumentException("Corpo da requisicao invalido.");
                }

                Avaliacao avaliacaoCriada = avaliacaoService.registrarAvaliacao(avaliacaoDaRequisicao);
                response.status(201); // 201 Created
                
                // Se não usar DTO, mude para: return gson.toJson(avaliacaoCriada);
                return gson.toJson(new AvaliacaoResponseDTO(avaliacaoCriada));
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor.");
            }
        });

        // R - READ (Lista): Busca avaliações por Evento usando Query Params (ex: /api/avaliacoes?eventoId=1)
        get("/api/avaliacoes", (request, response) -> {
            response.type("application/json");

            try {
                String eventoIdParam = request.queryParams("eventoId");
                if (eventoIdParam == null || eventoIdParam.trim().isEmpty()) {
                    response.status(400);
                    return errorJson("Forneca um parametro ?eventoId=");
                }

                Integer eventoId = Integer.parseInt(eventoIdParam);
                
                // Assumindo que o service chama o buscarPorEvento do DAO
                List<AvaliacaoResponseDTO> resultados = avaliacaoService.buscarPorEvento(eventoId).stream()
                        .map(AvaliacaoResponseDTO::new)
                        .collect(Collectors.toList());
                        
                response.status(200);
                return gson.toJson(resultados);
            } catch (NumberFormatException e) {
                response.status(400);
                return errorJson("O parametro eventoId deve ser um numero valido.");
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor.");
            }
        });

        // R - READ (Único): Busca avaliação específica por ID
        get("/api/avaliacoes/:id", (request, response) -> {
            response.type("application/json");

            try {
                Integer id = Integer.parseInt(request.params(":id"));
                Avaliacao avaliacao = avaliacaoService.buscarPorId(id);
                
                response.status(200);
                return gson.toJson(new AvaliacaoResponseDTO(avaliacao));
            } catch (NumberFormatException e) {
                response.status(400);
                return errorJson("ID invalido.");
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (NoSuchElementException e) {
                response.status(404); // 404 Not Found
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor.");
            }
        });

        // U - UPDATE: Atualiza uma avaliação existente
        put("/api/avaliacoes/:id", (request, response) -> {
            response.type("application/json");

            try {
                Integer id = Integer.parseInt(request.params(":id"));
                Avaliacao dadosAtualizados = gson.fromJson(request.body(), Avaliacao.class);
                
                if (dadosAtualizados == null) {
                    throw new IllegalArgumentException("Corpo da requisicao invalido.");
                }

                Avaliacao avaliacaoSalva = avaliacaoService.atualizarAvaliacao(id, dadosAtualizados);
                response.status(200);
                return gson.toJson(new AvaliacaoResponseDTO(avaliacaoSalva));
            } catch (NumberFormatException e) {
                response.status(400);
                return errorJson("ID invalido.");
            } catch (IllegalArgumentException e) {
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
        });

        // D - DELETE: Remove uma avaliação
        delete("/api/avaliacoes/:id", (request, response) -> {
            response.type("application/json");

            try {
                Integer id = Integer.parseInt(request.params(":id"));
                avaliacaoService.eliminarAvaliacao(id);
                
                response.status(204); // 204 No Content
                return "";
            } catch (NumberFormatException e) {
                response.status(400);
                return errorJson("ID invalido.");
            } catch (IllegalArgumentException e) {
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
        });
    }

    // Função auxiliar para padronizar o retorno de erros em JSON
    private String errorJson(String message) {
        return gson.toJson(Map.of("erro", message));
    }
}