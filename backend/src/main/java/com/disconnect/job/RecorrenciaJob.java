package com.disconnect.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.disconnect.service.EventoService;

/**
 * Job diario que recria eventos recorrentes: quando a ultima ocorrencia de um
 * evento SEMANAL/MENSAL/ANUAL ainda esta "Ativo" mas a data ja passou, a
 * ocorrencia e encerrada e uma nova e criada na proxima data da recorrencia.
 */
public class RecorrenciaJob {

    private final EventoService eventoService;
    private final ScheduledExecutorService scheduler;

    public RecorrenciaJob(EventoService eventoService) {
        this.eventoService = eventoService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "recorrencia-job");
            thread.setDaemon(true);
            return thread;
        });
    }

    /** Executa uma vez logo apos o arranque e depois a cada 24 horas. */
    public void iniciar() {
        scheduler.scheduleAtFixedRate(this::executar, 1, 24 * 60, TimeUnit.MINUTES);
    }

    private void executar() {
        try {
            int criados = eventoService.processarRecorrencias();
            if (criados > 0) {
                System.out.println("[RecorrenciaJob] " + criados + " evento(s) recorrente(s) recriado(s).");
            }
        } catch (Exception e) {
            System.err.println("[RecorrenciaJob] Erro ao processar recorrencias: " + e.getMessage());
        }
    }
}
