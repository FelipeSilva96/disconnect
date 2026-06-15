import type {
  CreateParticipacaoDTO,
  Participacao,
  ResponderParticipacaoDTO,
  UpdateParticipacaoDTO,
} from "@/types";
import { api } from "@/services/api";

export const participacaoService = {
  async listByEvento(eventoId: number): Promise<Participacao[]> {
    return api.get<Participacao[]>(`/participacoes?eventoId=${eventoId}`);
  },

  async listByUsuario(usuarioId: number): Promise<Participacao[]> {
    return api.get<Participacao[]>(`/participacoes?usuarioId=${usuarioId}`);
  },

  async create(
    dto: CreateParticipacaoDTO,
    usuarioId: number,
  ): Promise<Participacao> {
    return api.post<Participacao>("/participacoes", {
      id_evento: dto.eventoId,
      id_solicitante: usuarioId,
      mensagem_solicitacao: normalizeMessage(dto.mensagemSolicitacao),
    });
  },

  async update(
    eventoId: number,
    usuarioId: number,
    dto: UpdateParticipacaoDTO,
  ): Promise<Participacao> {
    return api.put<Participacao>(`/participacoes/${eventoId}/${usuarioId}`, {
      mensagem_solicitacao: normalizeMessage(dto.mensagemSolicitacao),
    });
  },

  async respond(
    eventoId: number,
    usuarioId: number,
    dto: ResponderParticipacaoDTO,
  ): Promise<Participacao> {
    return api.put<Participacao>(
      `/participacoes/${eventoId}/${usuarioId}/resposta`,
      {
        status: dto.status,
        mensagem_resposta: normalizeMessage(dto.mensagemResposta),
      },
    );
  },

  async approve(
    eventoId: number,
    usuarioId: number,
    mensagemResposta?: string,
  ): Promise<Participacao> {
    return this.respond(eventoId, usuarioId, {
      status: "APROVADO",
      mensagemResposta,
    });
  },

  async reject(
    eventoId: number,
    usuarioId: number,
    mensagemResposta?: string,
  ): Promise<Participacao> {
    return this.respond(eventoId, usuarioId, {
      status: "RECUSADO",
      mensagemResposta,
    });
  },

  async remove(eventoId: number, usuarioId: number): Promise<void> {
    await api.delete<void>(`/participacoes/${eventoId}/${usuarioId}`);
  },
};

function normalizeMessage(message?: string) {
  const trimmed = message?.trim();
  return trimmed || undefined;
}
