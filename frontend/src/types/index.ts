// --- Enums ---

export type FrequenciaEvento = "UNICO" | "SEMANAL" | "MENSAL" | "ANUAL";

export type StatusParticipacao = "PENDENTE" | "APROVADO" | "RECUSADO";

// --- Entities ---

export interface Usuario {
  id: number;
  nome: string;
  email?: string;
  login: string;
  dataNascimento?: string;
  idade?: number;
  biografia?: string;
  urlFoto?: string;
  localizacao?: string;
  hobbies?: string[];
  nivelExperiencia?: Record<string, string>;
  avaliacaoMedia?: number;
  totalAvaliacoes?: number;
  dataCriacao: string;
  token?: string;
}

export interface Categoria {
  id: number;
  nome: string;
  modalidade: string;
}

export interface Evento {
  id: number;
  nome: string;
  descricao?: string;
  dataEvento: string; // ISO date string
  local: string;
  latitude?: number;
  longitude?: number;
  cidade?: string;
  frequencia: FrequenciaEvento;
  fotoUrl?: string; // Cloudinary image URL
  diasDaSemana?: string[];
  diasDoMes?: number[];
  quantMinimaPessoas?: number;
  quantMaximaPessoas?: number;
  participantesInscritos?: number;
  modalidadeHobby?: string;
  nivelDeHabilidade?: string;
  status?: string;
  organizador: Usuario;
  categorias: Categoria[];
  dataCriacao: string;
}

export interface Participacao {
  idEvento: number;
  idSolicitante: number;
  solicitante: Usuario;
  evento: Evento;
  status: StatusParticipacao;
  dataSolicitacao: string;
  mensagemSolicitacao?: string;
  mensagemResposta?: string;
  dataResposta?: string;
}

export interface Avaliacao {
  id: number;
  avaliador?: Usuario;
  avaliadorId?: number;
  avaliado?: Usuario;
  avaliadoId?: number;
  evento?: Evento;
  eventoId?: number;
  nota: number; // 1-5
  comentario?: string;
  dataAvaliacao: string;
}

// --- DTOs (for create/update forms) ---

export interface CreateUsuarioDTO {
  nome: string;
  email: string;
  login: string;
  senha: string;
  dataNascimento?: string;
  biografia?: string;
  urlFoto?: string;
  localizacao?: string;
  hobbies?: string[];
  nivelExperiencia?: Record<string, string>;
}

export interface UpdateUsuarioDTO {
  nome?: string;
  email?: string;
  dataNascimento?: string;
  biografia?: string;
  urlFoto?: string;
  localizacao?: string;
  hobbies?: string[];
  nivelExperiencia?: Record<string, string>;
}

export interface LoginDTO {
  login: string;
  senha: string;
}

export interface CreateEventoDTO {
  nome: string;
  descricao: string;
  dataEvento: string;
  local: string;
  latitude?: number;
  longitude?: number;
  cidade?: string;
  frequencia: FrequenciaEvento;
  categoriaIds: number[];
  fotoUrl?: string;
  diasDaSemana?: string[];
  diasDoMes?: number[];
  quantMinimaPessoas: number;
  quantMaximaPessoas: number;
  participantesInscritos?: number;
  modalidadeHobby?: string;
  nivelDeHabilidade?: string;
  status?: string;
}

export interface UpdateEventoDTO {
  nome?: string;
  descricao?: string;
  dataEvento?: string;
  local?: string;
  latitude?: number;
  longitude?: number;
  cidade?: string;
  frequencia?: FrequenciaEvento;
  categoriaIds?: number[];
  fotoUrl?: string;
  diasDaSemana?: string[];
  diasDoMes?: number[];
  quantMinimaPessoas?: number;
  quantMaximaPessoas?: number;
  participantesInscritos?: number;
  modalidadeHobby?: string;
  nivelDeHabilidade?: string;
  status?: string;
}

export interface CreateAvaliacaoDTO {
  eventoId: number;
  avaliadoId: number;
  nota: number;
  comentario?: string;
}

export interface CreateParticipacaoDTO {
  eventoId: number;
  mensagemSolicitacao?: string;
}

export interface UpdateParticipacaoDTO {
  mensagemSolicitacao?: string;
}

export interface ResponderParticipacaoDTO {
  status: Exclude<StatusParticipacao, "PENDENTE">;
  mensagemResposta?: string;
}

// --- API response wrappers ---

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface EventoRecomendadoIA {
  evento: Evento;
  pontuacao: number;
  motivo: string;
  sinais: string[];
}

export interface RecomendacoesIAResponse {
  geradoPorIa: boolean;
  mensagem: string;
  recomendacoes: EventoRecomendadoIA[];
}

export interface RascunhoEventoIA {
  nome: string;
  descricao: string;
  dataEvento: string;
  local: string;
  frequencia: FrequenciaEvento;
  categoriaIds: number[];
  fotoUrl?: string;
  diasDaSemana?: string[];
  diasDoMes?: number[];
  quantMinimaPessoas: number;
  quantMaximaPessoas: number;
  modalidadeHobby?: string;
  nivelDeHabilidade?: string;
  status?: string;
  alertas?: string[];
  geradoPorIa?: boolean;
}
