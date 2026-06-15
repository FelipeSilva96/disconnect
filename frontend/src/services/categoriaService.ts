import type { Categoria } from "@/types";
import { api } from "@/services/api";

export const categoriaService = {
  async list(): Promise<Categoria[]> {
    return api.get<Categoria[]>("/categorias");
  },

  async getByNome(nome: string): Promise<Categoria[]> {
    const categorias = await api.get<Categoria[]>("/categorias");
    return categorias.filter(
      (c) => c.nome.toLowerCase() === nome.toLowerCase(),
    );
  },
};
