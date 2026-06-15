import { api } from "./api";
import type { Usuario, UpdateUsuarioDTO } from "@/types";

export const usuarioService = {
  async buscarPorId(id: number): Promise<Usuario | undefined> {
    try {
      return await api.get<Usuario>(`/usuarios/${id}`);
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

  /**
   * Rota de Front-end (Service) para atualizar os dados do utilizador.
   * Dispara um PUT para /api/usuarios/{id} no Back-end Java.
   */
  async atualizarPerfil(
    id: number,
    dados: UpdateUsuarioDTO,
    token?: string,
  ): Promise<Usuario> {
    return api.put<Usuario>(`/usuarios/${id}`, dados, {
      headers: authHeaders(token),
    });
  },

  /**
   * Rota de Front-end (Service) para apagar a conta definitivamente.
   * Dispara um DELETE para /api/usuarios/{id} no Back-end Java.
   */
  async deletarConta(id: number, token?: string): Promise<void> {
    return api.delete<void>(`/usuarios/${id}`, {
      headers: authHeaders(token),
    });
  },
};

function authHeaders(token?: string): HeadersInit {
  return token ? { Authorization: `Bearer ${token}` } : {};
}
