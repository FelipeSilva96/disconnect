import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { usuarioService } from "@/services/usuarioService";
import { categoriaService } from "@/services/categoriaService";
import { uploadImage } from "@/services/cloudinaryService";
import { LocationAutocomplete } from "@/components/LocationAutocomplete";
import type { Categoria } from "@/types";
import styles from "./Profile.module.css";

const NIVEIS = ["Iniciante", "Intermediário", "Avançado"];

interface HobbyEntry {
  hobby: string;
  nivel: string;
}

export function EditProfilePage() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [form, setForm] = useState({
    nome: user?.nome ?? "",
    email: user?.email ?? "",
    dataNascimento: user?.dataNascimento ?? "",
    biografia: user?.biografia ?? "",
    localizacao: user?.localizacao ?? "",
  });
  const [urlFoto, setUrlFoto] = useState(user?.urlFoto ?? "");
  const [uploading, setUploading] = useState(false);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [hobbies, setHobbies] = useState<HobbyEntry[]>(() =>
    (user?.hobbies ?? []).map((hobby) => ({
      hobby,
      nivel: user?.nivelExperiencia?.[hobby] ?? "",
    })),
  );
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    categoriaService
      .list()
      .then(setCategorias)
      .catch(() => setCategorias([]));
  }, []);

  const currentUser = user;

  if (!currentUser) {
    return null;
  }

  // Modalidades grouped by category for the hobby selects
  const categoriasAgrupadas = categorias.reduce<Map<string, Categoria[]>>(
    (groups, categoria) => {
      const list = groups.get(categoria.nome) ?? [];
      list.push(categoria);
      groups.set(categoria.nome, list);
      return groups;
    },
    new Map(),
  );

  function handleChange(
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  }

  async function handlePhotoChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      setError("A imagem deve ter no máximo 5MB.");
      return;
    }

    setError("");
    setUploading(true);
    try {
      const url = await uploadImage(file);
      setUrlFoto(url);
    } catch {
      setError("Erro ao fazer upload da foto. Tente novamente.");
    } finally {
      setUploading(false);
    }
  }

  function updateHobby(index: number, patch: Partial<HobbyEntry>) {
    setHobbies((previous) =>
      previous.map((entry, i) =>
        i === index ? { ...entry, ...patch } : entry,
      ),
    );
  }

  function addHobby() {
    setHobbies((previous) => [...previous, { hobby: "", nivel: "" }]);
  }

  function removeHobby(index: number) {
    setHobbies((previous) => previous.filter((_, i) => i !== index));
  }

  // Hobby options already picked in other rows can't be picked again
  function isHobbyTaken(hobby: string, index: number) {
    return hobbies.some((entry, i) => i !== index && entry.hobby === hobby);
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    const usuario = currentUser;
    if (!usuario) return;

    setError("");

    const dataNascimento = form.dataNascimento.trim() || undefined;
    if (dataNascimento) {
      const idade = calcularIdade(dataNascimento);
      if (idade === undefined || idade < 12) {
        setError("Informe uma data de nascimento válida a partir de 12 anos.");
        return;
      }
    }

    const hobbiesValidos = hobbies.filter((entry) => entry.hobby);
    const semNivel = hobbiesValidos.some((entry) => !entry.nivel);
    if (semNivel) {
      setError("Selecione o nível de experiência de cada hobby.");
      return;
    }

    if (!usuario.token) {
      setError("Sessão expirada. Entre novamente para editar o perfil.");
      return;
    }

    setSubmitting(true);
    try {
      const updatedUser = await usuarioService.atualizarPerfil(
        usuario.id,
        {
          nome: form.nome.trim(),
          email: form.email.trim(),
          dataNascimento,
          biografia: form.biografia.trim(),
          urlFoto: urlFoto.trim(),
          localizacao: form.localizacao.trim(),
          hobbies: hobbiesValidos.map((entry) => entry.hobby),
          nivelExperiencia: Object.fromEntries(
            hobbiesValidos.map((entry) => [entry.hobby, entry.nivel]),
          ),
        },
        usuario.token,
      );
      updateUser(updatedUser);
      navigate("/profile");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Erro ao atualizar perfil.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className={styles.page}>
      <div className={styles.formHeader}>
        <Link to="/profile" className={styles.backLink}>
          <span className="material-symbols-outlined">arrow_back</span>
          Voltar
        </Link>
        <h1>Editar perfil</h1>
      </div>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <p className={styles.error}>{error}</p>}

        {/* Photo upload (Cloudinary, same flow as events) */}
        <div className={styles.photoField}>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className={styles.hiddenInput}
            onChange={handlePhotoChange}
          />
          <button
            type="button"
            className={styles.photoPreview}
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            title="Alterar foto"
          >
            {urlFoto ? (
              <img src={urlFoto} alt="Foto de perfil" />
            ) : (
              <span className="material-symbols-outlined">add_a_photo</span>
            )}
          </button>
          <div>
            <strong>Foto de perfil</strong>
            <p className={styles.photoHint}>
              {uploading
                ? "Enviando foto..."
                : "Clique na imagem para enviar uma nova foto (máx. 5MB)."}
            </p>
            {urlFoto && (
              <button
                type="button"
                className={styles.removePhotoBtn}
                onClick={() => setUrlFoto("")}
              >
                Remover foto
              </button>
            )}
          </div>
        </div>

        <label>
          Nome
          <input
            type="text"
            name="nome"
            value={form.nome}
            onChange={handleChange}
            required
          />
        </label>

        <label>
          E-mail
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            required
          />
        </label>

        <label>
          Data de nascimento
          <input
            type="date"
            name="dataNascimento"
            value={form.dataNascimento}
            onChange={handleChange}
          />
        </label>

        <label>
          Localização (cidade)
          <LocationAutocomplete
            cityOnly
            value={form.localizacao}
            placeholder="Busque sua cidade..."
            onChangeText={(text) =>
              setForm((previous) => ({ ...previous, localizacao: text }))
            }
            onSelect={(place) =>
              setForm((previous) => ({
                ...previous,
                localizacao: place.city
                  ? `${place.city}${place.state ? `, ${place.state}` : ""}`
                  : place.label,
              }))
            }
          />
        </label>

        <label>
          Biografia
          <textarea
            name="biografia"
            rows={5}
            value={form.biografia}
            onChange={handleChange}
            maxLength={300}
          />
        </label>

        {/* Hobbies with experience level */}
        <div className={styles.hobbiesField}>
          <span className={styles.hobbiesLabel}>Hobbies e interesses</span>

          {hobbies.length === 0 && (
            <p className={styles.photoHint}>
              Adicione os hobbies que você pratica e seu nível em cada um.
            </p>
          )}

          {hobbies.map((entry, index) => (
            <div key={index} className={styles.hobbyRow}>
              <select
                value={entry.hobby}
                onChange={(e) => updateHobby(index, { hobby: e.target.value })}
                required
              >
                <option value="">Selecione um hobby</option>
                {[...categoriasAgrupadas.entries()].map(
                  ([categoria, modalidades]) => (
                    <optgroup key={categoria} label={categoria}>
                      {modalidades.map((modalidade) => (
                        <option
                          key={modalidade.id}
                          value={modalidade.modalidade}
                          disabled={isHobbyTaken(modalidade.modalidade, index)}
                        >
                          {modalidade.modalidade}
                        </option>
                      ))}
                    </optgroup>
                  ),
                )}
                {/* Keep legacy free-text hobbies selectable */}
                {entry.hobby &&
                  !categorias.some((c) => c.modalidade === entry.hobby) && (
                    <option value={entry.hobby}>{entry.hobby}</option>
                  )}
              </select>

              <select
                value={entry.nivel}
                onChange={(e) => updateHobby(index, { nivel: e.target.value })}
                required
              >
                <option value="">Nível</option>
                {NIVEIS.map((nivel) => (
                  <option key={nivel} value={nivel}>
                    {nivel}
                  </option>
                ))}
              </select>

              <button
                type="button"
                className={styles.removeHobbyBtn}
                onClick={() => removeHobby(index)}
                title="Remover hobby"
              >
                <span className="material-symbols-outlined">delete</span>
              </button>
            </div>
          ))}

          <button
            type="button"
            className={styles.addHobbyBtn}
            onClick={addHobby}
          >
            <span className="material-symbols-outlined">add</span>
            Adicionar hobby
          </button>
        </div>

        <button
          type="submit"
          className={styles.primaryButton}
          disabled={submitting || uploading}
        >
          <span className="material-symbols-outlined">save</span>
          {submitting ? "Salvando..." : "Salvar alterações"}
        </button>
      </form>
    </main>
  );
}

function calcularIdade(dataNascimento: string): number | undefined {
  const nascimento = new Date(`${dataNascimento}T00:00:00`);
  if (Number.isNaN(nascimento.getTime()) || nascimento > new Date()) {
    return undefined;
  }

  const hoje = new Date();
  let idade = hoje.getFullYear() - nascimento.getFullYear();
  const aniversarioAindaNaoChegou =
    hoje.getMonth() < nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() &&
      hoje.getDate() < nascimento.getDate());

  if (aniversarioAindaNaoChegou) {
    idade -= 1;
  }

  return idade;
}
