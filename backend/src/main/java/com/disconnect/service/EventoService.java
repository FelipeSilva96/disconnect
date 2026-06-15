package com.disconnect.service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import com.disconnect.dao.EventoDAO;
import com.disconnect.dao.UsuarioDAO;
import com.disconnect.domain.Evento;
import com.disconnect.domain.Modalidade;
import com.disconnect.domain.Usuario;
import com.disconnect.dto.EventoRequestDTO;

public class EventoService {

    private final EventoDAO eventoDAO;
    private final UsuarioDAO usuarioDAO;

    public EventoService() {
        this.eventoDAO = new EventoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public Evento criarEvento(Integer organizadorId, EventoRequestDTO dto) {
        if (organizadorId == null || organizadorId <= 0) {
            throw new IllegalArgumentException("O ID do organizador e obrigatorio.");
        }

        if (dto == null) {
            throw new IllegalArgumentException("Os dados do evento sao obrigatorios.");
        }

        Usuario organizador = usuarioDAO.buscarPorId(organizadorId);
        if (organizador == null) {
            throw new IllegalArgumentException("Organizador nao encontrado.");
        }

        String nome = requireText(dto.getNome(), "O nome do evento e obrigatorio.");
        String descricao = requireText(dto.getDescricao(), "A descricao do evento e obrigatoria.");
        String local = requireText(dto.getLocal(), "O local do evento e obrigatorio.");

        if (dto.getFrequencia() == null) {
            throw new IllegalArgumentException("A frequencia do evento e obrigatoria.");
        }

        LocalDateTime dataEvento = parseDataEvento(dto.getDataEvento());
        if (!dataEvento.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data do evento deve ser futura.");
        }

        Integer quantMinima = requirePositiveInteger(dto.getQuantMinimaPessoas(), "A quantidade minima de participantes e obrigatoria.");
        Integer quantMaxima = requirePositiveInteger(dto.getQuantMaximaPessoas(), "A quantidade maxima de participantes e obrigatoria.");

        if (quantMinima > quantMaxima) {
            throw new IllegalArgumentException("A quantidade maxima deve ser maior ou igual a minima.");
        }

        List<Integer> modalidadeIds = sanitizeModalidadeIds(dto.getModalidadeIds());
        if (modalidadeIds.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos uma modalidade.");
        }

        List<Modalidade> modalidades = eventoDAO.buscarModalidadesPorIds(modalidadeIds);
        if (modalidades.size() != modalidadeIds.size()) {
            throw new IllegalArgumentException("Uma ou mais modalidades informadas nao existem.");
        }

        Evento evento = new Evento();
        evento.setNome(nome);
        evento.setDescricao(descricao);
        evento.setDataEvento(dataEvento);
        evento.setLocalizacao(local);
        validarCoordenadas(dto.getLatitude(), dto.getLongitude());
        evento.setLatitude(dto.getLatitude());
        evento.setLongitude(dto.getLongitude());
        evento.setCidade(trimToNull(dto.getCidade()));
        evento.setFrequencia(dto.getFrequencia());
        evento.setUrlFoto(trimToNull(dto.getFotoUrl()));
        evento.setDiasDaSemana(sanitizeTextList(dto.getDiasDaSemana()));
        evento.setDiasDoMes(sanitizeDiasDoMes(dto.getDiasDoMes()));
        evento.setQuantMinimaPessoas(quantMinima);
        evento.setQuantMaximaPessoas(quantMaxima);
        evento.setNivelDeHabilidade(requireText(dto.getNivelDeHabilidade(), "O nivel de habilidade e obrigatorio."));
        evento.setStatus(trimToNull(dto.getStatus()) != null ? dto.getStatus().trim() : "Ativo");
        evento.setOrganizador(organizador);
        evento.setModalidades(modalidades);
        validarRecorrencia(evento);
        return eventoDAO.inserir(evento, modalidadeIds);
    }

    public Evento atualizarEvento(Integer id, EventoRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Os dados do evento sao obrigatorios.");
        }

        Evento evento = buscarPorId(id);

        if (dto.getNome() != null) {
            evento.setNome(requireText(dto.getNome(), "O nome do evento e obrigatorio."));
        }

        if (dto.getDescricao() != null) {
            evento.setDescricao(requireText(dto.getDescricao(), "A descricao do evento e obrigatoria."));
        }

        if (dto.getDataEvento() != null) {
            LocalDateTime dataEvento = parseDataEvento(dto.getDataEvento());
            if (!dataEvento.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("A data do evento deve ser futura.");
            }
            evento.setDataEvento(dataEvento);
        }

        if (dto.getLocal() != null) {
            evento.setLocalizacao(requireText(dto.getLocal(), "O local do evento e obrigatorio."));
        }

        if (dto.getLatitude() != null || dto.getLongitude() != null) {
            validarCoordenadas(dto.getLatitude(), dto.getLongitude());
            evento.setLatitude(dto.getLatitude());
            evento.setLongitude(dto.getLongitude());
        }

        if (dto.getCidade() != null) {
            evento.setCidade(trimToNull(dto.getCidade()));
        }

        if (dto.getFrequencia() != null) {
            evento.setFrequencia(dto.getFrequencia());
        }

        if (dto.getFotoUrl() != null) {
            evento.setUrlFoto(trimToNull(dto.getFotoUrl()));
        }

        if (dto.getDiasDaSemana() != null) {
            evento.setDiasDaSemana(sanitizeTextList(dto.getDiasDaSemana()));
        }

        if (dto.getDiasDoMes() != null) {
            evento.setDiasDoMes(sanitizeDiasDoMes(dto.getDiasDoMes()));
        }

        if (dto.getQuantMinimaPessoas() != null) {
            evento.setQuantMinimaPessoas(requirePositiveInteger(dto.getQuantMinimaPessoas(), "A quantidade minima de participantes e obrigatoria."));
        }

        if (dto.getQuantMaximaPessoas() != null) {
            evento.setQuantMaximaPessoas(requirePositiveInteger(dto.getQuantMaximaPessoas(), "A quantidade maxima de participantes e obrigatoria."));
        }

        if (evento.getQuantMinimaPessoas() != null && evento.getQuantMaximaPessoas() != null
                && evento.getQuantMinimaPessoas() > evento.getQuantMaximaPessoas()) {
            throw new IllegalArgumentException("A quantidade maxima deve ser maior ou igual a minima.");
        }

        if (dto.getNivelDeHabilidade() != null) {
            evento.setNivelDeHabilidade(requireText(dto.getNivelDeHabilidade(), "O nivel de habilidade e obrigatorio."));
        }

        if (dto.getStatus() != null) {
            evento.setStatus(requireText(dto.getStatus(), "O status do evento e obrigatorio."));
        }

        List<Integer> modalidadeIds = null;
        if (dto.getModalidadeIds() != null) {
            modalidadeIds = sanitizeModalidadeIds(dto.getModalidadeIds());
            if (modalidadeIds.isEmpty()) {
                throw new IllegalArgumentException("Selecione ao menos uma modalidade.");
            }

            List<Modalidade> modalidades = eventoDAO.buscarModalidadesPorIds(modalidadeIds);
            if (modalidades.size() != modalidadeIds.size()) {
                throw new IllegalArgumentException("Uma ou mais modalidades informadas nao existem.");
            }
            evento.setModalidades(modalidades);
        }

        validarRecorrencia(evento);

        Evento eventoAtualizado = eventoDAO.atualizar(evento, modalidadeIds);
        if (eventoAtualizado == null) {
            throw new RuntimeException("Falha ao atualizar o evento.");
        }

        return eventoAtualizado;
    }

    public void eliminarEvento(Integer id) {
        buscarPorId(id);

        boolean sucesso = eventoDAO.deletar(id);
        if (!sucesso) {
            throw new RuntimeException("Falha ao eliminar o evento.");
        }
    }

    public Evento buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID do evento fornecido e invalido.");
        }

        Evento evento = eventoDAO.buscarPorId(id);
        if (evento == null) {
            throw new NoSuchElementException("Evento com o ID " + id + " nao foi encontrado.");
        }

        return evento;
    }

    public List<Evento> listarTodos() {
        return eventoDAO.listarTodos();
    }

    public List<Evento> listarPorOrganizador(Integer organizadorId) {
        if (organizadorId == null || organizadorId <= 0) {
            throw new IllegalArgumentException("O ID do organizador fornecido e invalido.");
        }
        return eventoDAO.listarPorOrganizador(organizadorId);
    }

    public List<Modalidade> listarModalidades() {
        return eventoDAO.listarModalidades();
    }

    /**
     * Recria eventos recorrentes cuja ultima ocorrencia (ainda "Ativo") ja passou.
     * A ocorrencia passada e marcada como "Encerrado" e uma nova e criada com a
     * proxima data da recorrencia. Retorna a quantidade de eventos recriados.
     */
    public int processarRecorrencias() {
        LocalDateTime agora = LocalDateTime.now();
        int criados = 0;

        for (Evento evento : eventoDAO.listarTodos()) {
            try {
                if (evento.getFrequencia() == null || evento.getFrequencia().name().equals("UNICO")) {
                    continue;
                }

                if (evento.getStatus() == null || !evento.getStatus().equalsIgnoreCase("Ativo")) {
                    continue;
                }

                if (evento.getDataEvento() == null || !evento.getDataEvento().isBefore(agora)) {
                    continue;
                }

                LocalDateTime proximaData = calcularProximaOcorrencia(evento, agora);
                if (proximaData == null) {
                    continue;
                }

                // Encerra a ocorrencia passada antes de criar a proxima,
                // garantindo que o job nunca a reprocesse.
                evento.setStatus("Encerrado");
                eventoDAO.atualizar(evento, null);

                Evento proxima = copiarParaProximaOcorrencia(evento, proximaData);
                List<Integer> modalidadeIds = evento.getModalidades().stream()
                        .map(Modalidade::getId)
                        .collect(Collectors.toList());
                eventoDAO.inserir(proxima, modalidadeIds);
                criados++;
            } catch (Exception e) {
                System.err.println("[RecorrenciaJob] Falha ao recriar evento " + evento.getId()
                        + ": " + e.getMessage());
            }
        }

        return criados;
    }

    private Evento copiarParaProximaOcorrencia(Evento original, LocalDateTime proximaData) {
        Evento copia = new Evento();
        copia.setNome(original.getNome());
        copia.setDescricao(original.getDescricao());
        copia.setDataEvento(proximaData);
        copia.setLocalizacao(original.getLocalizacao());
        copia.setLatitude(original.getLatitude());
        copia.setLongitude(original.getLongitude());
        copia.setCidade(original.getCidade());
        copia.setFrequencia(original.getFrequencia());
        copia.setUrlFoto(original.getUrlFoto());
        copia.setDiasDaSemana(new ArrayList<>(original.getDiasDaSemana()));
        copia.setDiasDoMes(new ArrayList<>(original.getDiasDoMes()));
        copia.setQuantMinimaPessoas(original.getQuantMinimaPessoas());
        copia.setQuantMaximaPessoas(original.getQuantMaximaPessoas());
        copia.setNivelDeHabilidade(original.getNivelDeHabilidade());
        copia.setStatus("Ativo");
        copia.setOrganizador(original.getOrganizador());
        copia.setModalidades(new ArrayList<>(original.getModalidades()));
        return copia;
    }

    private LocalDateTime calcularProximaOcorrencia(Evento evento, LocalDateTime referencia) {
        LocalTime horario = evento.getDataEvento().toLocalTime();

        switch (evento.getFrequencia()) {
            case SEMANAL:
                return proximaDataSemanal(evento.getDiasDaSemana(), referencia, horario);
            case MENSAL:
                return proximaDataMensal(evento.getDiasDoMes(), referencia, horario);
            case ANUAL: {
                LocalDateTime proxima = evento.getDataEvento();
                while (!proxima.isAfter(referencia)) {
                    proxima = proxima.plusYears(1);
                }
                return proxima;
            }
            default:
                return null;
        }
    }

    private LocalDateTime proximaDataSemanal(List<String> diasDaSemana, LocalDateTime referencia, LocalTime horario) {
        List<DayOfWeek> dias = diasDaSemana.stream()
                .map(this::parseDiaDaSemana)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (dias.isEmpty()) {
            // sem dias informados, repete uma semana depois da referencia
            return referencia.toLocalDate().plusWeeks(1).atTime(horario);
        }

        LocalDate dia = referencia.toLocalDate();
        for (int i = 0; i < 8; i++) {
            LocalDateTime candidata = dia.atTime(horario);
            if (candidata.isAfter(referencia) && dias.contains(dia.getDayOfWeek())) {
                return candidata;
            }
            dia = dia.plusDays(1);
        }
        return null;
    }

    private LocalDateTime proximaDataMensal(List<Integer> diasDoMes, LocalDateTime referencia, LocalTime horario) {
        if (diasDoMes.isEmpty()) {
            return referencia.toLocalDate().plusMonths(1).atTime(horario);
        }

        LocalDate dia = referencia.toLocalDate();
        // procura ate dois meses a frente (cobre dias que nao existem em todo mes, ex: 31)
        for (int i = 0; i < 62; i++) {
            LocalDateTime candidata = dia.atTime(horario);
            if (candidata.isAfter(referencia) && diasDoMes.contains(dia.getDayOfMonth())) {
                return candidata;
            }
            dia = dia.plusDays(1);
        }
        return null;
    }

    private DayOfWeek parseDiaDaSemana(String nome) {
        if (nome == null) {
            return null;
        }

        String normalizado = Normalizer.normalize(nome.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        Map<String, DayOfWeek> dias = Map.of(
                "segunda", DayOfWeek.MONDAY,
                "terca", DayOfWeek.TUESDAY,
                "quarta", DayOfWeek.WEDNESDAY,
                "quinta", DayOfWeek.THURSDAY,
                "sexta", DayOfWeek.FRIDAY,
                "sabado", DayOfWeek.SATURDAY,
                "domingo", DayOfWeek.SUNDAY);

        for (Map.Entry<String, DayOfWeek> entry : dias.entrySet()) {
            if (normalizado.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private Integer requirePositiveInteger(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private LocalDateTime parseDataEvento(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A data do evento e obrigatoria.");
        }

        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data do evento invalido.");
        }
    }

    private List<Integer> sanitizeModalidadeIds(List<Integer> categoriaIds) {
        if (categoriaIds == null) {
            return new ArrayList<>();
        }

        return categoriaIds.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new));
    }

    private List<String> sanitizeTextList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new));
    }

    private List<Integer> sanitizeDiasDoMes(List<Integer> diasDoMes) {
        if (diasDoMes == null) {
            return new ArrayList<>();
        }

        List<Integer> diasSanitizados = diasDoMes.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new));

        boolean existeDiaInvalido = diasSanitizados.stream().anyMatch(dia -> dia < 1 || dia > 31);
        if (existeDiaInvalido) {
            throw new IllegalArgumentException("Os dias do mes devem estar entre 1 e 31.");
        }

        return diasSanitizados;
    }

    private void validarCoordenadas(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude e longitude devem ser informadas juntas.");
        }

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas do evento invalidas.");
        }
    }

    private void validarRecorrencia(Evento evento) {
        if (evento.getFrequencia() == null) {
            throw new IllegalArgumentException("A frequencia do evento e obrigatoria.");
        }

        if (evento.getFrequencia().name().equals("SEMANAL") && evento.getDiasDaSemana().isEmpty()) {
            throw new IllegalArgumentException("Eventos semanais precisam informar ao menos um dia da semana.");
        }

        if (evento.getFrequencia().name().equals("MENSAL") && evento.getDiasDoMes().isEmpty()) {
            throw new IllegalArgumentException("Eventos mensais precisam informar ao menos um dia do mes.");
        }
    }
}
