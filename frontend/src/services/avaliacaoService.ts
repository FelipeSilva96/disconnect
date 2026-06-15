import type { Avaliacao, CreateAvaliacaoDTO } from "@/types";
import { api } from "@/services/api";

export const avaliacaoService = {
  async listByEvento(eventoId: number): Promise<Avaliacao[]> {
    return api.get<Avaliacao[]>(`/avaliacoes?eventoId=${eventoId}`);
  },

  async create(
    dto: CreateAvaliacaoDTO,
    avaliadorId: number,
  ): Promise<Avaliacao> {
    return api.post<Avaliacao>("/avaliacoes", {
      avaliador: { id: avaliadorId },
      avaliado: { id: dto.avaliadoId },
      evento: { id: dto.eventoId },
      nota: dto.nota,
      comentario: dto.comentario?.trim() || undefined,
    });
  },
};
