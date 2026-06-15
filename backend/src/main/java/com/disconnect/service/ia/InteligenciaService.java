package com.disconnect.service.ia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.disconnect.dao.EventoDAO;
import com.disconnect.dao.UsuarioDAO;
import com.disconnect.domain.Evento;
import com.disconnect.domain.Modalidade;
import com.disconnect.domain.Usuario;
import com.disconnect.domain.enums.FrequenciaEvento;
import com.disconnect.dto.EventoResponseDTO;
import com.disconnect.dto.ia.EventoRecomendadoDTO;
import com.disconnect.dto.ia.RascunhoEventoIaResponseDTO;
import com.disconnect.dto.ia.RecomendacaoEventoResponseDTO;
import com.disconnect.util.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InteligenciaService {

    private static final int LIMITE_PADRAO = 3;
    private static final int LIMITE_MAXIMO = 8;

    private final EventoDAO eventoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ClaudeApiService claudeApiService;
    private final Gson gson;

    public InteligenciaService(Gson gson) {
        this.eventoDAO = new EventoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.claudeApiService = new ClaudeApiService(gson);
        this.gson = gson;
    }

    public RecomendacaoEventoResponseDTO recomendarEventos(Integer usuarioId, Integer limiteSolicitado) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("Usuario autenticado invalido.");
        }

        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado.");
        }

        int limite = normalizarLimite(limiteSolicitado);
        List<Evento> eventosCandidatos = listarEventosCandidatos(usuarioId);

        if (eventosCandidatos.isEmpty()) {
            return new RecomendacaoEventoResponseDTO(false, "Nenhum evento futuro disponivel para recomendar.",
                    List.of());
        }

        if (isMockEnabled()) {
            return new RecomendacaoEventoResponseDTO(
                    false,
                    "Modo demonstracao ativo. Recomendacoes geradas localmente sem consumir creditos de IA.",
                    recomendarLocalmente(usuario, eventosCandidatos, limite));
        }

        if (!claudeApiService.isConfigured()) {
            return new RecomendacaoEventoResponseDTO(
                    false,
                    "A chave da Anthropic nao esta configurada. Usando recomendacao local temporaria.",
                    recomendarLocalmente(usuario, eventosCandidatos, limite));
        }

        try {
            String respostaTexto = claudeApiService.enviarMensagem(
                    systemPromptRecomendacao(limite),
                    userPromptRecomendacao(usuario, eventosCandidatos, limite),
                    1400);

            List<EventoRecomendadoDTO> recomendacoes = montarRecomendacoesDaIa(respostaTexto, eventosCandidatos,
                    limite);

            if (recomendacoes.isEmpty()) {
                return new RecomendacaoEventoResponseDTO(
                        false,
                        "A IA nao retornou recomendacoes validas. Usando recomendacao local temporaria.",
                        recomendarLocalmente(usuario, eventosCandidatos, limite));
            }

            return new RecomendacaoEventoResponseDTO(
                    true,
                    "Recomendacoes geradas com Claude AI com base no seu perfil e nos eventos disponiveis.",
                    recomendacoes);
        } catch (RuntimeException e) {
            return new RecomendacaoEventoResponseDTO(
                    false,
                    "Nao foi possivel chamar a IA agora. Usando recomendacao local temporaria.",
                    recomendarLocalmente(usuario, eventosCandidatos, limite));
        }
    }

    public RascunhoEventoIaResponseDTO gerarRascunhoEvento(String textoLivre) {
        String texto = requireText(textoLivre, "Descreva a ideia do evento para a IA.");
        List<Modalidade> modalidades = eventoDAO.listarModalidades();

        if (isMockEnabled()) {
            return gerarRascunhoMockado(texto, modalidades);
        }

        if (!claudeApiService.isConfigured()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY nao configurada no servidor. Configure a chave para gerar eventos com IA.");
        }

        String respostaTexto = claudeApiService.enviarMensagem(
                systemPromptRascunhoEvento(),
                userPromptRascunhoEvento(texto, modalidades),
                1500);

        return montarRascunhoDaIa(respostaTexto, modalidades);
    }

    private boolean isMockEnabled() {
        return AppConfig.getBoolean("ai.mock.enabled", false);
    }

    private int normalizarLimite(Integer limiteSolicitado) {
        if (limiteSolicitado == null || limiteSolicitado <= 0) {
            return LIMITE_PADRAO;
        }
        return Math.min(limiteSolicitado, LIMITE_MAXIMO);
    }

    private List<Evento> listarEventosCandidatos(Integer usuarioId) {
        LocalDateTime agora = LocalDateTime.now();

        return eventoDAO.listarTodos().stream()
                .filter(evento -> evento.getDataEvento() != null && evento.getDataEvento().isAfter(agora))
                .filter(evento -> evento.getOrganizador() == null || !usuarioId.equals(evento.getOrganizador().getId()))
                .filter(evento -> evento.getStatus() == null || "Ativo".equalsIgnoreCase(evento.getStatus()))
                .limit(60)
                .collect(Collectors.toList());
    }

    private String systemPromptRecomendacao(int limite) {
        return "Voce e o motor de recomendacao inteligente do <dis>connect, uma plataforma para conectar pessoas em eventos presenciais. "
                + "Analise o perfil do usuario e os eventos disponiveis. Recomende ate " + limite + " eventos. "
                + "Use criterios de hobbies, modalidade, categoria, localizacao, nivel de habilidade, descricao, seguranca e proximidade semantica. "
                + "Nao invente eventos e nao use IDs que nao estejam na lista. "
                + "Responda somente com JSON puro, sem markdown, no formato: "
                + "{\"recomendacoes\":[{\"eventoId\":1,\"pontuacao\":95,\"motivo\":\"texto curto em pt-BR\",\"sinais\":[\"motivo 1\",\"motivo 2\"]}]}";
    }

    private String userPromptRecomendacao(Usuario usuario, List<Evento> eventos, int limite) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("limite", limite);
        payload.put("usuario", usuarioParaContexto(usuario));
        payload.put("eventos", eventos.stream().map(this::eventoParaContexto).collect(Collectors.toList()));
        return gson.toJson(payload);
    }

    private Map<String, Object> usuarioParaContexto(Usuario usuario) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", usuario.getId());
        map.put("nome", usuario.getNome());
        map.put("idade", usuario.getIdade());
        map.put("biografia", usuario.getBiografia());
        map.put("localizacao", usuario.getLocalizacao());
        map.put("hobbies", usuario.getHobbies());
        map.put("nivelExperiencia", usuario.getNivelExperiencia());
        map.put("avaliacaoMedia", usuario.getAvaliacaoMedia());
        return map;
    }

    private Map<String, Object> eventoParaContexto(Evento evento) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", evento.getId());
        map.put("nome", evento.getNome());
        map.put("descricao", evento.getDescricao());
        map.put("dataEvento", evento.getDataEvento() != null ? evento.getDataEvento().toString() : null);
        map.put("local", evento.getLocalizacao());
        map.put("frequencia", evento.getFrequencia() != null ? evento.getFrequencia().name() : null);
        map.put("nivelDeHabilidade", evento.getNivelDeHabilidade());
        map.put("quantMinimaPessoas", evento.getQuantMinimaPessoas());
        map.put("quantMaximaPessoas", evento.getQuantMaximaPessoas());
        map.put("organizadorId", evento.getOrganizador() != null ? evento.getOrganizador().getId() : null);
        map.put("modalidades", evento.getModalidades() != null
                ? evento.getModalidades().stream().map(this::modalidadeParaContexto).collect(Collectors.toList())
                : List.of());
        return map;
    }

    private Map<String, Object> modalidadeParaContexto(Modalidade modalidade) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", modalidade.getId());
        map.put("nome", modalidade.getNome());
        map.put("categoria", modalidade.getCategoria());
        return map;
    }

    private List<EventoRecomendadoDTO> montarRecomendacoesDaIa(String respostaTexto, List<Evento> eventosCandidatos,
            int limite) {
        Map<Integer, Evento> eventosPorId = eventosCandidatos.stream()
                .collect(Collectors.toMap(Evento::getId, evento -> evento));

        JsonObject root = parseJsonObject(respostaTexto);
        JsonArray recomendacoesJson = root.has("recomendacoes") && root.get("recomendacoes").isJsonArray()
                ? root.getAsJsonArray("recomendacoes")
                : new JsonArray();

        List<EventoRecomendadoDTO> recomendacoes = new ArrayList<>();
        for (JsonElement item : recomendacoesJson) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject obj = item.getAsJsonObject();
            Integer eventoId = getInt(obj, "eventoId");
            Evento evento = eventosPorId.get(eventoId);
            if (evento == null) {
                continue;
            }

            Integer pontuacao = clamp(getInt(obj, "pontuacao"), 0, 100, 70);
            String motivo = getString(obj, "motivo", "Evento recomendado com base no seu perfil.");
            List<String> sinais = getStringList(obj, "sinais");

            recomendacoes.add(new EventoRecomendadoDTO(new EventoResponseDTO(evento), pontuacao, motivo, sinais));

            if (recomendacoes.size() >= limite) {
                break;
            }
        }

        return recomendacoes;
    }

    private List<EventoRecomendadoDTO> recomendarLocalmente(Usuario usuario, List<Evento> eventos, int limite) {
        return eventos.stream()
                .map(evento -> pontuarLocalmente(usuario, evento))
                .sorted(Comparator.comparing(EventoRecomendadoDTO::getPontuacao).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    private EventoRecomendadoDTO pontuarLocalmente(Usuario usuario, Evento evento) {
        int score = 40;
        List<String> sinais = new ArrayList<>();

        List<String> hobbies = usuario.getHobbies() != null ? usuario.getHobbies() : List.of();
        List<String> modalidades = evento.getModalidades() != null
                ? evento.getModalidades().stream().map(Modalidade::getNome).filter(Objects::nonNull)
                        .collect(Collectors.toList())
                : List.of();

        boolean hobbyCombina = hobbies.stream().anyMatch(hobby -> modalidades.stream()
                .anyMatch(modalidade -> normalizar(hobby).contains(normalizar(modalidade))
                        || normalizar(modalidade).contains(normalizar(hobby))));

        if (hobbyCombina) {
            score += 30;
            sinais.add("modalidade parecida com seus hobbies");
        }

        if (usuario.getLocalizacao() != null && evento.getLocalizacao() != null
                && normalizar(evento.getLocalizacao()).contains(normalizar(usuario.getLocalizacao()))) {
            score += 15;
            sinais.add("localizacao compativel com seu perfil");
        }

        if (evento.getNivelDeHabilidade() != null && usuario.getNivelExperiencia() != null
                && usuario.getNivelExperiencia().values().stream()
                        .anyMatch(nivel -> normalizar(nivel).equals(normalizar(evento.getNivelDeHabilidade())))) {
            score += 15;
            sinais.add("nivel de habilidade compativel");
        }

        score = Math.min(score, 100);
        if (sinais.isEmpty()) {
            sinais.add("evento futuro disponivel na plataforma");
        }

        String motivo = "Este evento foi selecionado por compatibilidade entre seu perfil e os dados cadastrados do evento.";
        return new EventoRecomendadoDTO(new EventoResponseDTO(evento), score, motivo, sinais);
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String systemPromptRascunhoEvento() {
        return "Voce e um assistente de criacao de eventos do <dis>connect. "
                + "Transforme a descricao livre do usuario em um rascunho estruturado de evento. "
                + "Escolha uma modalidade existente a partir da lista enviada. "
                + "Use somente uma destas frequencias: UNICO, SEMANAL, MENSAL, ANUAL. "
                + "Use somente um destes niveis: Iniciante, Intermediario, Avancado. "
                + "Se alguma informacao estiver ausente, use um valor plausivel e escreva um alerta. "
                + "A dataEvento deve ser futura e vir no formato ISO local yyyy-MM-ddTHH:mm:ss. "
                + "Responda somente com JSON puro, sem markdown, no formato: "
                + "{\"nome\":\"...\",\"descricao\":\"...\",\"dataEvento\":\"2026-06-20T14:00:00\",\"local\":\"...\",\"frequencia\":\"UNICO\",\"categoriaIds\":[1],\"fotoUrl\":null,\"diasDaSemana\":[],\"diasDoMes\":[],\"quantMinimaPessoas\":4,\"quantMaximaPessoas\":20,\"modalidadeHobby\":\"Corrida\",\"nivelDeHabilidade\":\"Iniciante\",\"status\":\"Ativo\",\"alertas\":[\"...\"]}";
    }

    private String userPromptRascunhoEvento(String textoLivre, List<Modalidade> modalidades) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dataAtualServidor", LocalDateTime.now().toString());
        payload.put("descricaoLivreDoUsuario", textoLivre);
        payload.put("modalidadesDisponiveis",
                modalidades.stream().map(this::modalidadeParaContexto).collect(Collectors.toList()));
        return gson.toJson(payload);
    }

    private RascunhoEventoIaResponseDTO montarRascunhoDaIa(String respostaTexto, List<Modalidade> modalidades) {
        JsonObject root = parseJsonObject(respostaTexto);
        Map<Integer, Modalidade> modalidadePorId = modalidades.stream()
                .collect(Collectors.toMap(Modalidade::getId, m -> m));

        RascunhoEventoIaResponseDTO dto = new RascunhoEventoIaResponseDTO();
        dto.setNome(getString(root, "nome", "Evento criado com IA"));
        dto.setDescricao(getString(root, "descricao", "Descrição gerada automaticamente pela IA."));
        dto.setDataEvento(
                getString(root, "dataEvento", LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).toString()));
        dto.setLocal(getString(root, "local", "Local a definir"));
        dto.setFrequencia(parseFrequencia(getString(root, "frequencia", "UNICO")));
        dto.setCategoriaIds(getIntegerList(root, "categoriaIds"));
        dto.setFotoUrl(null);
        dto.setDiasDaSemana(getStringList(root, "diasDaSemana"));
        dto.setDiasDoMes(getIntegerList(root, "diasDoMes"));
        dto.setQuantMinimaPessoas(clamp(getInt(root, "quantMinimaPessoas"), 1, 999, 4));
        dto.setQuantMaximaPessoas(clamp(getInt(root, "quantMaximaPessoas"), dto.getQuantMinimaPessoas(), 999, 20));
        dto.setNivelDeHabilidade(normalizarNivel(getString(root, "nivelDeHabilidade", "Iniciante")));
        dto.setStatus("Ativo");
        dto.setAlertas(getStringList(root, "alertas"));
        dto.setGeradoPorIa(true);

        if (dto.getCategoriaIds().isEmpty() || !modalidadePorId.containsKey(dto.getCategoriaIds().get(0))) {
            Modalidade primeira = modalidades.isEmpty() ? null : modalidades.get(0);
            if (primeira != null) {
                dto.setCategoriaIds(List.of(primeira.getId()));
                dto.getAlertas().add("A IA nao escolheu uma modalidade valida; foi aplicada uma modalidade padrao.");
            }
        }

        if (!dto.getCategoriaIds().isEmpty()) {
            Modalidade modalidade = modalidadePorId.get(dto.getCategoriaIds().get(0));
            if (modalidade != null) {
                dto.setModalidadeHobby(modalidade.getNome());
            }
        }

        if (dto.getQuantMaximaPessoas() < dto.getQuantMinimaPessoas()) {
            dto.setQuantMaximaPessoas(dto.getQuantMinimaPessoas());
        }

        return dto;
    }

    private RascunhoEventoIaResponseDTO gerarRascunhoMockado(String textoLivre, List<Modalidade> modalidades) {
        Modalidade modalidade = escolherModalidadeMock(textoLivre, modalidades);

        LocalDateTime dataSugerida = LocalDateTime.now()
                .plusDays(7)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        String modalidadeNome = modalidade != null ? modalidade.getNome() : "Atividade";
        String nome = montarNomeMock(textoLivre, modalidadeNome);

        RascunhoEventoIaResponseDTO dto = new RascunhoEventoIaResponseDTO();

        dto.setNome(nome);
        dto.setDescricao(montarDescricaoMock(textoLivre, modalidadeNome));
        dto.setDataEvento(dataSugerida.toString());
        dto.setLocal(inferirLocalMock(textoLivre));
        dto.setFrequencia(FrequenciaEvento.UNICO);
        dto.setCategoriaIds(modalidade != null ? List.of(modalidade.getId()) : new ArrayList<>());
        dto.setFotoUrl(null);
        dto.setDiasDaSemana(new ArrayList<>());
        dto.setDiasDoMes(new ArrayList<>());
        dto.setQuantMinimaPessoas(inferirQuantidadeMinimaMock(textoLivre));
        dto.setQuantMaximaPessoas(inferirQuantidadeMaximaMock(textoLivre));
        dto.setModalidadeHobby(modalidadeNome);
        dto.setNivelDeHabilidade(inferirNivelMock(textoLivre));
        dto.setStatus("Ativo");
        dto.setGeradoPorIa(false);

        dto.setAlertas(new ArrayList<>(List.of(
                "Modo demonstracao ativo: este rascunho foi gerado localmente e nao consumiu creditos da Anthropic.",
                "Revise data, horario, local e modalidade antes de criar o evento.")));

        if (dto.getQuantMaximaPessoas() < dto.getQuantMinimaPessoas()) {
            dto.setQuantMaximaPessoas(dto.getQuantMinimaPessoas() + 10);
        }

        return dto;
    }

    private Modalidade escolherModalidadeMock(String textoLivre, List<Modalidade> modalidades) {
        if (modalidades == null || modalidades.isEmpty()) {
            return null;
        }

        String texto = normalizar(textoLivre);

        for (Modalidade modalidade : modalidades) {
            String nome = normalizar(modalidade.getNome());
            String categoria = normalizar(modalidade.getCategoria());

            if (!nome.isBlank() && texto.contains(nome)) {
                return modalidade;
            }

            if (!categoria.isBlank() && texto.contains(categoria)) {
                return modalidade;
            }
        }

        Map<String, List<String>> palavrasPorModalidade = new LinkedHashMap<>();

        palavrasPorModalidade.put("corrida", List.of("corrida", "correr", "run", "pampulha", "maratona"));
        palavrasPorModalidade.put("caminhada", List.of("caminhada", "caminhar", "trilha", "passeio"));
        palavrasPorModalidade.put("futebol", List.of("futebol", "pelada", "bola", "society"));
        palavrasPorModalidade.put("volei", List.of("volei", "vôlei", "volleyball"));
        palavrasPorModalidade.put("basquete", List.of("basquete", "basket"));
        palavrasPorModalidade.put("estudo",
                List.of("estudo", "estudar", "ads", "programacao", "programação", "faculdade"));
        palavrasPorModalidade.put("musica", List.of("musica", "música", "violao", "violão", "banda"));
        palavrasPorModalidade.put("cinema", List.of("cinema", "filme", "filmes"));
        palavrasPorModalidade.put("leitura", List.of("leitura", "livro", "clube do livro"));

        for (Map.Entry<String, List<String>> entry : palavrasPorModalidade.entrySet()) {
            boolean encontrouPalavra = entry.getValue().stream().anyMatch(texto::contains);

            if (!encontrouPalavra) {
                continue;
            }

            String alvo = normalizar(entry.getKey());

            for (Modalidade modalidade : modalidades) {
                String nome = normalizar(modalidade.getNome());
                String categoria = normalizar(modalidade.getCategoria());

                if (nome.contains(alvo) || alvo.contains(nome) || categoria.contains(alvo)) {
                    return modalidade;
                }
            }
        }

        return modalidades.get(0);
    }

    private String montarNomeMock(String textoLivre, String modalidadeNome) {
        String texto = textoLivre.trim();

        if (texto.length() > 65) {
            texto = texto.substring(0, 65).trim();
        }

        if (texto.isBlank()) {
            return "Encontro de " + modalidadeNome;
        }

        String primeiraLetra = texto.substring(0, 1).toUpperCase();
        String restante = texto.length() > 1 ? texto.substring(1) : "";

        return primeiraLetra + restante;
    }

    private String montarDescricaoMock(String textoLivre, String modalidadeNome) {
        return "Rascunho gerado em modo demonstracao a partir da descricao: \""
                + textoLivre.trim()
                + "\". A proposta foi classificada como uma atividade relacionada a "
                + modalidadeNome
                + ". Revise as informacoes antes de publicar.";
    }

    private String inferirLocalMock(String textoLivre) {
        String texto = normalizar(textoLivre);

        if (texto.contains("pampulha")) {
            return "Lagoa da Pampulha - Belo Horizonte";
        }

        if (texto.contains("puc")
                || texto.contains("coreu")
                || texto.contains("coracao eucaristico")
                || texto.contains("coração eucarístico")) {
            return "PUC Minas - Coração Eucarístico";
        }

        if (texto.contains("praca") || texto.contains("praça")) {
            return "Praça a definir";
        }

        if (texto.contains("parque")) {
            return "Parque a definir";
        }

        return "Local a definir";
    }

    private String inferirNivelMock(String textoLivre) {
        String texto = normalizar(textoLivre);

        if (texto.contains("avancado")
                || texto.contains("avançado")
                || texto.contains("experiente")) {
            return "Avançado";
        }

        if (texto.contains("intermediario")
                || texto.contains("intermediário")) {
            return "Intermediário";
        }

        return "Iniciante";
    }

    private Integer inferirQuantidadeMinimaMock(String textoLivre) {
        String texto = normalizar(textoLivre);

        if (texto.contains("dupla") || texto.contains("duplas")) {
            return 2;
        }

        if (texto.contains("grupo pequeno")) {
            return 3;
        }

        return 4;
    }

    private Integer inferirQuantidadeMaximaMock(String textoLivre) {
        String texto = normalizar(textoLivre);

        if (texto.contains("20")) {
            return 20;
        }

        if (texto.contains("10")) {
            return 10;
        }

        if (texto.contains("grupo pequeno")) {
            return 8;
        }

        return 20;
    }

    private JsonObject parseJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Resposta vazia da IA.");
        }

        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```[a-zA-Z]*", "").replaceFirst("```$", "").trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("A IA nao retornou um objeto JSON valido.");
        }

        return JsonParser.parseString(cleaned.substring(start, end + 1)).getAsJsonObject();
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private Integer getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private String getString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        String value = obj.get(key).getAsString();
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private List<String> getStringList(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonArray()) {
            return new ArrayList<>();
        }

        List<String> values = new ArrayList<>();
        for (JsonElement item : obj.getAsJsonArray(key)) {
            if (!item.isJsonNull()) {
                values.add(item.getAsString());
            }
        }
        return values;
    }

    private List<Integer> getIntegerList(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonArray()) {
            return new ArrayList<>();
        }

        List<Integer> values = new ArrayList<>();
        for (JsonElement item : obj.getAsJsonArray(key)) {
            try {
                values.add(item.getAsInt());
            } catch (Exception ignored) {
            }
        }
        return values;
    }

    private Integer clamp(Integer value, int min, int max, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, value));
    }

    private FrequenciaEvento parseFrequencia(String value) {
        try {
            return FrequenciaEvento.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return FrequenciaEvento.UNICO;
        }
    }

    private String normalizarNivel(String value) {
        if (value == null) {
            return "Iniciante";
        }
        String normalizado = value.trim().toLowerCase();
        if (normalizado.contains("avanc") || normalizado.contains("avanç")) {
            return "Avançado";
        }
        if (normalizado.contains("inter")) {
            return "Intermediário";
        }
        return "Iniciante";
    }
}
