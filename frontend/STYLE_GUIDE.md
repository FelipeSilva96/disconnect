# &lt;dis&gt;connect — Guia de Estilo & Boas Práticas (Frontend)

> Documento auxiliar para a construção das páginas React/TypeScript do projeto **disconnect**.

---

## 1. Visão Geral da Arquitetura

```
frontend/src/
├── components/         # Componentes reutilizáveis (Header, BottomNav, EventCard, etc.)
│   └── NomeComponente/
│       ├── NomeComponente.tsx
│       ├── NomeComponente.module.css
│       └── index.ts
├── contexts/           # React Contexts (AuthContext, etc.)
├── layouts/            # Layouts de página (AppLayout)
├── mocks/              # Dados mock para desenvolvimento sem backend
├── pages/              # Páginas da aplicação
│   └── NomePagina/
│       ├── NomePagina.tsx
│       ├── NomePagina.module.css
│       └── index.ts
├── services/           # Camada de serviço (chamadas à API / mocks)
├── styles/             # Estilos globais e design tokens
├── types/              # Tipos TypeScript (espelham o domínio Java)
├── App.tsx             # Rotas da aplicação
└── main.tsx            # Entry point
```

### Princípios

- **Colocation**: cada componente/página tem seus arquivos (TSX, CSS Module, barrel export) na mesma pasta.
- **Barrel exports**: toda pasta expõe um `index.ts` com `export { X } from './X'`.
- **Path aliases**: use `@/` para imports absolutos (configurado no tsconfig e no vite.config).

---

## 2. Design Tokens (CSS Custom Properties)

Todos os tokens ficam em `src/styles/global.css` sob `:root`. **Nunca use valores "mágicos" diretamente no CSS** — sempre referencie variáveis.

### 2.1 Cores

| Token                    | Valor     | Uso                            |
| ------------------------ | --------- | ------------------------------ |
| `--color-primary`        | `#000000` | Botões primários, texto forte  |
| `--color-primary-hover`  | `#1a1a1a` | Hover de botões primários      |
| `--color-accent`         | `#a8d5ba` | Destaque, badges, hover        |
| `--color-accent-dark`    | `#7cc49a` | Destaques, ícones de feature   |
| `--color-success`        | `#28a745` | Status aprovado, confirmações  |
| `--color-success-bg`     | `#f0faf4` | Fundo suave de sucesso         |
| `--color-danger`         | `#dc3545` | Erros, botões destrutivos      |
| `--color-danger-hover`   | `#c82333` | Hover de botões destrutivos    |
| `--color-danger-bg`      | `#fff5f5` | Fundo suave de erro/perigo     |
| `--color-danger-border`  | `#ffd6d6` | Borda de painéis de perigo     |
| `--color-warning`        | `#ffc107` | Status pendente                |
| `--color-warning-bg`     | `#fff8e1` | Fundo suave de aviso           |
| `--color-warning-border` | `#ffe69c` | Borda de painéis de aviso      |
| `--color-warning-text`   | `#b8860b` | Texto/ícone sobre fundo aviso  |
| `--color-info`           | `#0d6efd` | Links informativos             |
| `--color-rating`         | `#f5b301` | Estrelas de avaliação          |
| `--color-white`          | `#ffffff` | Texto sobre fundo escuro       |
| `--color-bg`             | `#f8f9fa` | Background geral               |
| `--color-bg-card`        | `#ffffff` | Background de cards            |
| `--color-border`         | `#e9ecef` | Bordas sutis                   |
| `--color-text`           | `#212529` | Texto principal                |
| `--color-text-secondary` | `#6c757d` | Texto secundário               |
| `--color-text-muted`     | `#adb5bd` | Texto desabilitado/placeholder |

### 2.2 Tipografia

- **Fonte principal**: `Inter` (import via Google Fonts no `index.html`)
- **Ícones**: `Material Symbols Outlined` (Google Fonts)
- Tamanhos: `--font-size-xs` (0.75rem) até `--font-size-3xl` (2rem)

### 2.3 Espaçamento

Use a escala de `--space-1` (0.25rem) a `--space-12` (3rem). Mantenha consistência:

- Padding interno de cards: `--space-5`
- Gap entre cards em grid: `--space-5`
- Margem entre seções: `--space-8` ou `--space-12`

### 2.4 Bordas e Sombras

- Borda padrão: `1px solid var(--color-border)`
- Border radius de cards: `var(--radius-lg)` (12px)
- Border radius de inputs/botões: `var(--radius-md)` (8px)
- Border radius de badges/tags: `var(--radius-full)` (pill)
- Hover de card: `box-shadow: var(--shadow-md); transform: translateY(-2px)`

---

## 3. Componentes — Padrões

### 3.1 Estrutura de um Componente

```tsx
// src/components/MeuComponente/MeuComponente.tsx
import styles from "./MeuComponente.module.css";

interface MeuComponenteProps {
  titulo: string;
  // ...
}

export function MeuComponente({ titulo }: MeuComponenteProps) {
  return (
    <div className={styles.container}>
      <h2 className={styles.title}>{titulo}</h2>
    </div>
  );
}
```

### 3.2 Regras

1. **Function components** — nunca use `class`.
2. **Named exports** — nunca `export default` (exceto `App`).
3. **Props tipadas** via `interface` no mesmo arquivo.
4. **CSS Modules** para escopo local — nome do arquivo: `NomeComponente.module.css`.
5. **Sem lógica de negócio no componente** — delegue para services.

### 3.3 Ícones

Use Material Symbols diretamente:

```tsx
<span className="material-symbols-outlined">home</span>
```

Referência de nomes: https://fonts.google.com/icons

---

## 4. Páginas — Padrões

### 4.1 Estrutura de uma Página

```tsx
// src/pages/MinhaPagina/MinhaPagina.tsx
import { useEffect, useState } from "react";
import { eventoService } from "@/services/eventoService";
import styles from "./MinhaPagina.module.css";

export function MinhaPagina() {
  const [data, setData] = useState(/* ... */);

  useEffect(() => {
    eventoService.list().then(setData);
  }, []);

  return (
    <div>
      <h1 className={styles.title}>Título da Página</h1>
      {/* conteúdo */}
    </div>
  );
}
```

### 4.2 Regras

1. Cada página fica em `src/pages/NomePagina/`.
2. O título principal da página é sempre um `<h1>`.
3. Loading states sempre visíveis (texto ou skeleton).
4. Erros exibidos em `<p>` com classe de erro.

---

## 5. Services — Camada de Dados

### 5.1 Arquitetura Mock → API Real

Atualmente os services importam de `@/mocks/data` e simulam delays. Quando a API Java/Spring Boot estiver pronta:

1. Troque os imports de mock pelos `api.get(...)`, `api.post(...)` de `@/services/api.ts`.
2. O `vite.config.ts` já faz proxy de `/api` → `http://localhost:8080`.
3. **Não mude as assinaturas dos serviços** — as páginas não precisam saber se os dados são mock ou reais.

### 5.2 Exemplo de migração

```tsx
// ANTES (mock)
import { mockEventos } from '@/mocks/data'
async list(): Promise<Evento[]> {
  await delay()
  return [...mockEventos]
}

// DEPOIS (API real)
import { api } from './api'
async list(): Promise<Evento[]> {
  return api.get<Evento[]>('/eventos')
}
```

---

## 6. Tipos — Sincronização com Backend

Os tipos em `src/types/index.ts` espelham as classes Java em `com.disconnect.domain`:

| Java                 | TypeScript           | Observação                                   |
| -------------------- | -------------------- | -------------------------------------------- |
| `Usuario`            | `Usuario`            | `senha` omitida em leituras                  |
| `Evento`             | `Evento`             | `organizador` embutido, `categorias[]`       |
| `Categoria`          | `Categoria`          | —                                            |
| `Participacao`       | `Participacao`       | `StatusParticipacao` como union type         |
| `Avaliacao`          | `Avaliacao`          | `nota` de 1 a 5                              |
| `FrequenciaEvento`   | `FrequenciaEvento`   | Union: `UNICO \| SEMANAL \| MENSAL \| ANUAL` |
| `StatusParticipacao` | `StatusParticipacao` | Union: `PENDENTE \| APROVADO \| RECUSADO`    |

**Se o backend mudar um campo, atualize `types/index.ts` primeiro.**

---

## 7. Rotas da Aplicação

| Rota               | Página                   | Acesso      |
| ------------------ | ------------------------ | ----------- |
| `/landing`         | Landing                  | Público     |
| `/login`           | → Redireciona `/landing` | —           |
| `/register`        | → Redireciona `/landing` | —           |
| `/`                | Home                     | Autenticado |
| `/search`          | Search                   | Autenticado |
| `/events/create`   | CreateEvent              | Autenticado |
| `/events/:id/edit` | EditEvent                | Autenticado |
| `/events/:id`      | EventDetails             | Autenticado |
| `/events`          | MyEvents                 | Autenticado |
| `/profile`         | Profile (próprio)        | Autenticado |
| `/profile/:id`     | Profile (outro)          | Autenticado |

> **Login e Register não são rotas independentes.** São modais (`<Modal>`) exibidos sobre a Landing page. Os botões "Entrar" e "Comece agora" na Landing controlam `useState` para abrir/fechar os modais. Os links "Cadastre-se" / "Entrar" no rodapé de cada formulário alternam entre os modais.

---

## 8. Estilo CSS — Boas Práticas

### 8.1 CSS Modules

- Um arquivo `.module.css` por componente/página.
- **Nunca use estilos globais** para coisas específicas de um componente.
- Classes compostas: `className={\`${styles.item} ${isActive ? styles.active : ''}\`}`

### 8.2 Responsividade

- **Mobile-first**: escreva o CSS base para mobile, use `@media (min-width: 768px)` para desktop.
- Breakpoints:
  - `360px` — mobile pequeno (oculta nome do usuário no Header)
  - `480px` — mobile médio (compacta Header)
  - `576px` — mobile largo (ajustes de grid)
  - `768px` — tablet/desktop
  - `1024px` — desktop largo
  - `1200px` — desktop extra largo (padding generoso)
- O bottom nav é fixo no mobile (icon-only, sem labels), vira sidebar no desktop.
- O Header exibe menu dropdown com ícone de usuário + nome + chevron (inspirado no projeto original).

### 8.3 Padrão de Botões

```css
/* Primário */
.btnPrimary {
  padding: var(--space-3) var(--space-8);
  background: var(--color-primary);
  color: var(--color-white);
  font-weight: 700;
  border-radius: var(--radius-md);
}

/* Secundário (outline) */
.btnSecondary {
  padding: var(--space-3) var(--space-8);
  border: 2px solid var(--color-primary);
  color: var(--color-primary);
  font-weight: 700;
  border-radius: var(--radius-md);
}

/* Destrutivo */
.btnDanger {
  padding: var(--space-3) var(--space-8);
  background: var(--color-danger);
  color: var(--color-white);
  font-weight: 700;
  border-radius: var(--radius-md);
}
```

### 8.4 Padrão de Inputs

```css
.input {
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
}

.input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.08);
}
```

### 8.5 Padrão de Cards

```css
.card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast);
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
```

---

## 9. Convenções de Código

### 9.1 Nomenclatura

- **Arquivos**: `PascalCase.tsx` para componentes/páginas, `camelCase.ts` para services/utils.
- **Variáveis/funções**: `camelCase`.
- **Tipos/Interfaces**: `PascalCase`.
- **CSS classes**: `camelCase` (CSS Modules cuida do escopo).
- **Pastas de página**: `PascalCase` para pages, `camelCase` para o resto.

### 9.2 Imports

Ordem preferida:

1. React / bibliotecas externas
2. Types (`@/types`)
3. Contexts (`@/contexts`)
4. Services (`@/services`)
5. Components (`@/components`)
6. Styles (CSS Modules)

### 9.3 Estado

- **Estado local** (`useState`) para forms e UI.
- **AuthContext** para dados do usuário logado.
- Evite prop drilling excessivo — crie contextos quando necessário.

---

## 10. Checklist para Nova Página

- [ ] Criar pasta `src/pages/NomePagina/`
- [ ] Criar `NomePagina.tsx` com named export
- [ ] Criar `NomePagina.module.css` com estilos usando design tokens
- [ ] Criar `index.ts` com barrel export
- [ ] Adicionar rota em `App.tsx`
- [ ] Verificar se precisa de proteção (rota autenticada?)
- [ ] Ligar ao service correspondente (mock por enquanto)
- [ ] Testar responsividade (mobile → desktop)
- [ ] Incluir loading/error states

---

## 11. Checklist para Novo Componente

- [ ] Criar pasta `src/components/NomeComponente/`
- [ ] Definir `interface NomeComponenteProps` no arquivo TSX
- [ ] Usar CSS Modules para estilização
- [ ] Barrel export via `index.ts`
- [ ] Usar apenas design tokens no CSS
- [ ] Testar com dados variados (texto longo, vazio, etc.)

---

## 12. Fluxo de Autenticação (Mock)

1. Usuário acessa `/landing` → clica em "Entrar" ou "Comece agora"
2. Modal de Login ou Register abre sobre a Landing (componente `<Modal>`)
3. Formulário chama `authService.login()` ou `authService.register()`
4. Mock valida apenas o `login` (sem senha real)
5. `AuthContext` salva o usuário em `sessionStorage`
6. Após sucesso, `navigate('/')` leva às rotas protegidas
7. Rotas protegidas conferem `isAuthenticated` via `ProtectedRoute`
8. Logout limpa `sessionStorage` e redireciona para `/landing`

> O componente `<Modal>` (`src/components/Modal/`) é reutilizável: aceita `open`, `onClose` e `children`. Fecha com Esc ou clique no overlay.

**Na migração para API real**: substituir `authService.login()` por chamada POST ao backend (que retornará um JWT ou session cookie).

---

## 13. Próximos Passos (Páginas a construir)

| Prioridade | Página       | Status    |
| ---------- | ------------ | --------- |
| 1          | Landing      | ✅ Pronta |
| 2          | Login        | ✅ Pronta |
| 3          | Register     | ✅ Pronta |
| 4          | Home         | ✅ Pronta |
| 5          | Search       | ✅ Pronta |
| 6          | MyEvents     | ✅ Básico |
| 7          | EventDetails | 🔧 Stub   |
| 8          | CreateEvent  | 🔧 Stub   |
| 9          | EditEvent    | 🔧 Stub   |
| 10         | Profile      | 🔧 Stub   |

Stubs estão funcionais mas precisam de UI completa — implemente seguindo os padrões acima.
