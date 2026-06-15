package com.disconnect.controller;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.disconnect.dto.CategoriaResponseDTO;
import com.disconnect.dto.EventoRequestDTO;
import com.disconnect.dto.EventoResponseDTO;
import com.disconnect.service.EventoService;
import com.google.gson.Gson;

public class EventoController {

    private final EventoService eventoService;
    private final Gson gson;

    public EventoController(EventoService eventoService, Gson gson) {
        this.eventoService = eventoService;
        this.gson = gson;
    }

    public void registerRoutes() {
        get("/api/categorias", (request, response) -> {
            response.type("application/json");

            try {
                List<CategoriaResponseDTO> responseDtos = eventoService.listarModalidades().stream()
                        .map(CategoriaResponseDTO::new)
                        .collect(Collectors.toList());

                response.status(200);
                return gson.toJson(responseDtos);
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor: " + e);
            }
        });

        post("/api/eventos", (request, response) -> {
            response.type("application/json");

            try {
                String organizadorParam = request.queryParams("organizadorId");
                if (organizadorParam == null || organizadorParam.isBlank()) {
                    throw new IllegalArgumentException("O parametro organizadorId e obrigatorio.");
                }

                EventoRequestDTO dto = gson.fromJson(request.body(), EventoRequestDTO.class);
                if (dto == null) {
                    throw new IllegalArgumentException("Corpo da requisicao invalido.");
                }

                Integer organizadorId = Integer.parseInt(organizadorParam);
                EventoResponseDTO responseDto = new EventoResponseDTO(eventoService.criarEvento(organizadorId, dto));
                response.status(201);
                return gson.toJson(responseDto);
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor: " + e);
            }
        });

        get("/api/eventos", (request, response) -> {
            response.type("application/json");

            try {
                String organizadorParam = request.queryParams("organizadorId");
                List<EventoResponseDTO> responseDtos;

                if (organizadorParam != null && !organizadorParam.isBlank()) {
                    Integer organizadorId = Integer.parseInt(organizadorParam);
                    responseDtos = eventoService.listarPorOrganizador(organizadorId).stream()
                            .map(EventoResponseDTO::new)
                            .collect(Collectors.toList());
                } else {
                    responseDtos = eventoService.listarTodos().stream()
                            .map(EventoResponseDTO::new)
                            .collect(Collectors.toList());
                }

                response.status(200);
                return gson.toJson(responseDtos);
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor:" + e);
            }
        });

        get("/api/eventos/:id", (request, response) -> {
            response.type("application/json");

            try {
                Integer id = Integer.parseInt(request.params(":id"));
                EventoResponseDTO responseDto = new EventoResponseDTO(eventoService.buscarPorId(id));
                response.status(200);
                return gson.toJson(responseDto);
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

        put("/api/eventos/:id", (request, response) -> {
            response.type("application/json");

            try {
                Integer id = Integer.parseInt(request.params(":id"));
                EventoRequestDTO dto = gson.fromJson(request.body(), EventoRequestDTO.class);
                if (dto == null) {
                    throw new IllegalArgumentException("Corpo da requisicao invalido.");
                }

                EventoResponseDTO responseDto = new EventoResponseDTO(eventoService.atualizarEvento(id, dto));
                response.status(200);
                return gson.toJson(responseDto);
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (NoSuchElementException e) {
                response.status(404);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor: " + e);
            }
        });

        delete("/api/eventos/:id", (request, response) -> {
            response.type("application/json");

            try {
                Integer id = Integer.parseInt(request.params(":id"));
                eventoService.eliminarEvento(id);
                response.status(204);
                return "";
            } catch (IllegalArgumentException e) {
                response.status(400);
                return errorJson(e.getMessage());
            } catch (NoSuchElementException e) {
                response.status(404);
                return errorJson(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return errorJson("Erro interno do servidor: " + e);
            }
        });
    }

    private String errorJson(String message) {
        return gson.toJson(Map.of("erro", message));
    }
}