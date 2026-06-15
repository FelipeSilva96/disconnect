import type { Usuario, LoginDTO, CreateUsuarioDTO } from "@/types";
import { api } from "./api";

export const authService = {
  // REGISTRO
  //  Pega o DTO da tela e atira para o Java em POST /api/usuarios.
  // Se o Java devolver 201 Created, ele retorna o UsuarioResponseDTO.
  //  Se o Java devolver 400 ou 500, o api.ts lança o Erro e a tela captura.

  async register(data: CreateUsuarioDTO): Promise<Usuario> {
    return api.post<Usuario>("/usuarios", data);
  },

  // LOGIN:
  // Envia as credenciais para o servidor validar a senha.

  async login(credentials: LoginDTO): Promise<Usuario> {
    return api.post<Usuario>("/login", credentials);
  },
};
