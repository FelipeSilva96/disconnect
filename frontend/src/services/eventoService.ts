import type { Evento, CreateEventoDTO, UpdateEventoDTO } from "@/types";
import { api } from "@/services/api";

export const eventoService = {
  async list(): Promise<Evento[]> {
    return api.get<Evento[]>("/eventos");
  },

  async getById(id: number): Promise<Evento | undefined> {
    try {
      return await api.get<Evento>(`/eventos/${id}`);
    } catch (error) {
      if (
        error instanceof Error &&
        (error.message.includes("nao foi encontrado") ||
          error.message.includes("não foi encontrado") ||
          error.message.includes("HTTP Error 404"))
      ) {
        return undefined;
      }

      throw error;
    }
  },

  async listByOrganizador(organizadorId: number): Promise<Evento[]> {
    return api.get<Evento[]>(`/eventos?organizadorId=${organizadorId}`);
  },

  async create(dto: CreateEventoDTO, organizadorId: number): Promise<Evento> {
    return api.post<Evento>(`/eventos?organizadorId=${organizadorId}`, dto);
  },

  async update(id: number, dto: UpdateEventoDTO): Promise<Evento> {
    return api.put<Evento>(`/eventos/${id}`, dto);
  },

  async remove(id: number): Promise<void> {
    await api.delete<void>(`/eventos/${id}`);
  },
};
