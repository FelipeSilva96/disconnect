import { useAuth } from "@/contexts/AuthContext";
import { eventoService } from "@/services/eventoService";
import { iaService } from "@/services/iaService";
import type { CreateEventoDTO, RascunhoEventoIA } from "@/types";
import { EventForm } from "./EventForm";

export function CreateEventPage() {
  const { user } = useAuth();

  async function handleCreate(dto: CreateEventoDTO) {
    if (!user) {
      throw new Error("Usuário não autenticado.");
    }

    await eventoService.create(dto, user.id);
  }

  async function handleGenerateDraft(texto: string): Promise<RascunhoEventoIA> {
    if (!user) {
      throw new Error("Usuário não autenticado.");
    }

    return iaService.gerarRascunhoEvento(texto, user.token);
  }

  return (
    <EventForm
      title="Criar Novo Evento"
      onSubmit={handleCreate}
      onGenerateAiDraft={handleGenerateDraft}
      submitLabel="Criar Evento"
      successMessage="Evento criado com sucesso!"
      errorMessage="Erro ao criar evento. Tente novamente."
      redirectTo="/events"
    />
  );
}
