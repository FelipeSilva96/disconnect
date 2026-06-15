package app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import com.disconnect.controller.AuthController;
import com.disconnect.controller.AvaliacaoController;
import com.disconnect.controller.EventoController;
import com.disconnect.controller.InteligenciaController;
import com.disconnect.controller.ParticipacaoController;
import com.disconnect.controller.UsuarioController;
import com.disconnect.job.RecorrenciaJob;
import com.disconnect.service.AvaliacaoService;
import com.disconnect.service.EventoService;
import com.disconnect.service.ParticipacaoService;
import com.disconnect.service.UsuarioService;
import com.disconnect.service.ia.InteligenciaService;
import com.disconnect.util.AppConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;

public class Aplicacao {

    public static void main(String[] args) {
        int appPort = AppConfig.getInt("PORT", AppConfig.getInt("app.port", 8080));
        String allowedOrigin = AppConfig.get("app.cors.allowed-origin", "http://localhost:5173");
        Gson gson = createGson();

        port(appPort);
        enableCors(allowedOrigin);
        before((request, response) -> response.type("application/json"));

        UsuarioService usuarioService = new UsuarioService();
        EventoService eventoService = new EventoService();
        AvaliacaoService avaliacaoService = new AvaliacaoService();
        ParticipacaoService participacaoService = new ParticipacaoService();
        InteligenciaService inteligenciaService = new InteligenciaService(gson);

        new AuthController(usuarioService, gson).registerRoutes();
        new UsuarioController(usuarioService, gson).registerRoutes();
        new EventoController(eventoService, gson).registerRoutes();
        new AvaliacaoController(avaliacaoService, gson).registerRoutes();
        new ParticipacaoController(participacaoService, gson).registerRoutes();
        new InteligenciaController(inteligenciaService, gson).registerRoutes();

        new RecorrenciaJob(eventoService).iniciar();

        get("/health", (request, response) -> {
            response.status(200);
            return gson.toJson(Map.of("status", "ok"));
        });

        get("/favicon.ico", (request, response) -> {
            response.status(204);
            return "";
        });

        awaitInitialization();
        System.out.println("Servidor Spark iniciado na porta " + appPort + ".");
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                                .parse(json.getAsString()))
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
                .create();
    }

    private static void enableCors(String allowedOrigin) {
        options("/*", (request, response) -> {
            String requestHeaders = request.headers("Access-Control-Request-Headers");
            if (requestHeaders != null) {
                response.raw().setHeader("Access-Control-Allow-Headers", requestHeaders);
            }

            String requestMethod = request.headers("Access-Control-Request-Method");
            if (requestMethod != null) {
                response.raw().setHeader("Access-Control-Allow-Methods", requestMethod);
            }

            response.raw().setHeader("Access-Control-Allow-Origin", allowedOrigin);
            return "OK";
        });

        before((request, response) -> {
            response.raw().setHeader("Access-Control-Allow-Origin", allowedOrigin);
            response.raw().setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.raw().setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
    }
}