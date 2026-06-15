import { api } from "@/services/api";
import type { RascunhoEventoIA, RecomendacoesIAResponse } from "@/types";

function authHeaders(token?: string): HeadersInit {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const iaService = {
  async recomendarEventos(
    token?: string,
    limite = 3,
  ): Promise<RecomendacoesIAResponse> {
    return api.post<RecomendacoesIAResponse>(
      "/ia/eventos/recomendacoes",
      { limite },
      { headers: authHeaders(token) },
    );
  },

  async gerarRascunhoEvento(
    texto: string,
    token?: string,
  ): Promise<RascunhoEventoIA> {
    return api.post<RascunhoEventoIA>(
      "/ia/eventos/rascunho",
      { texto },
      { headers: authHeaders(token) },
    );
  },
};
