package com.disconnect.controller;

import static spark.Spark.post;

import java.util.Map;

import com.disconnect.dto.ia.RascunhoEventoIaRequestDTO;
import com.disconnect.dto.ia.RecomendacaoEventoRequestDTO;
import com.disconnect.service.ia.InteligenciaService;
import com.disconnect.util.AuthTokenUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class InteligenciaController {

    private final InteligenciaService inteligenciaService;
    private final Gson gson;

    public InteligenciaController(InteligenciaService inteligenciaService, Gson gson) {
        this.inteligenciaService = inteligenciaService;
        this.gson = gson;
    }

    public void registerRoutes() {
        post("/api/ia/eventos/recomendacoes", (request, response) -> {
            response.type("application/json");

            try {
                Integer usuarioId = AuthTokenUtil.validarCabecalhoAuthorization(request.headers("Authorization"));
                RecomendacaoEventoRequestDTO dto = gson.fromJson(request.body(), RecomendacaoEventoRequestDTO.class);

                response.status(200);
                return gson.toJson(inteligenciaService.recomendarEventos(
                        usuarioId,
                        dto != null ? dto.getLimite() : null));
            } catch (SecurityException e) {
                response.status(403);
                return errorJson(e.getMessage());
            } catch (IllegalArgumentException | JsonSyntaxException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno ao gerar recomendacoes inteligentes.");
            }
        });

        post("/api/ia/eventos/rascunho", (request, response) -> {
            response.type("application/json");

            try {
                AuthTokenUtil.validarCabecalhoAuthorization(request.headers("Authorization"));

                RascunhoEventoIaRequestDTO dto = gson.fromJson(request.body(), RascunhoEventoIaRequestDTO.class);
                if (dto == null) {
                    throw new IllegalArgumentException("Corpo da requisicao invalido.");
                }

                response.status(200);
                return gson.toJson(inteligenciaService.gerarRascunhoEvento(dto.getTexto()));
            } catch (SecurityException e) {
                response.status(403);
                return errorJson(e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException | JsonSyntaxException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno ao gerar rascunho de evento com IA.");
            }
        });
    }

    private String errorJson(String message) {
        return gson.toJson(Map.of("erro", message));
    }
}