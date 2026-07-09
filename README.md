<div align="center">

# &lt;dis&gt;connect

### Uma rede social pensada para aproximar pessoas fora das telas.

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=000)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-6-646CFF?style=for-the-badge&logo=vite&logoColor=white)

</div>

---

## 📌 Sobre o projeto

O **&lt;dis&gt;connect** é um projeto acadêmico desenvolvido com a ideia de criar uma rede social diferente das redes tradicionais. Em vez de incentivar o usuário a passar mais tempo rolando uma tela, a proposta é ajudar pessoas a encontrarem **eventos, atividades, encontros e experiências presenciais**, incentivando conexões reais fora do ambiente digital.

A aplicação permite que usuários criem um perfil, cadastrem eventos, encontrem atividades compatíveis com seus interesses, solicitem participação em eventos de outras pessoas e interajam com a plataforma de forma simples. A ideia central é usar a tecnologia como um meio para gerar encontros no mundo real, e não como um fim em si mesma.

Este repositório está organizado em duas partes principais:

- **`backend/`**: API em Java responsável pelas regras de negócio, autenticação, persistência em banco de dados e integração opcional com IA.
- **`frontend/`**: interface web em React + TypeScript, responsável pela experiência visual e interação do usuário.

---

## 🎯 Objetivo

O objetivo do projeto é oferecer uma plataforma onde o usuário possa:

- criar uma conta e manter um perfil pessoal;
- cadastrar informações como hobbies, localização e nível de experiência;
- visualizar eventos disponíveis;
- criar, editar e excluir eventos;
- solicitar participação em eventos;
- aprovar ou recusar solicitações de participação;
- avaliar usuários/eventos;
- receber recomendações de eventos com base no próprio perfil;
- gerar rascunhos de eventos com auxílio de IA, quando configurado.

---

## 🧠 Como funciona

De forma geral, o fluxo da aplicação é este:

1. O usuário acessa a aplicação pelo frontend.
2. Na landing page, ele pode criar uma conta ou fazer login.
3. Depois de autenticado, ele entra na área principal da aplicação.
4. A Home exibe eventos cadastrados na plataforma.
5. O usuário pode criar seus próprios eventos ou solicitar participação em eventos de outras pessoas.
6. O organizador do evento pode aceitar ou recusar solicitações.
7. O backend salva usuários, eventos, participações e avaliações no PostgreSQL.
8. A aplicação também possui recursos opcionais de IA para recomendações e geração de rascunhos de eventos.

---

## 🛠️ Tecnologias utilizadas

### Backend

- **Java 17**
- **Maven**
- **Spark Java** para criação da API HTTP
- **PostgreSQL** como banco de dados
- **JDBC** para acesso ao banco
- **Flyway** para controle de migrations
- **Gson** para serialização e desserialização JSON
- **BCrypt** para criptografia de senhas
- **Token HMAC** simples para autenticação
- **Integração opcional com Anthropic/Claude** para recursos de IA

### Frontend

- **React 19**
- **TypeScript**
- **Vite**
- **React Router DOM**
- **Leaflet / React Leaflet** para recursos de mapa/localização
- **CSS Modules**
- **ESLint**

---

## 📁 Estrutura do projeto

```text
disconnect/
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   ├── app/
│   │       │   │   └── Aplicacao.java
│   │       │   └── com/disconnect/
│   │       │       ├── controller/
│   │       │       ├── dao/
│   │       │       ├── domain/
│   │       │       ├── dto/
│   │       │       ├── service/
│   │       │       ├── util/
│   │       │       └── job/
│   │       └── resources/
│   │           └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
└── frontend/
    ├── public/
    ├── src/
    │   ├── components/
    │   ├── contexts/
    │   ├── layouts/
    │   ├── pages/
    │   ├── services/
    │   ├── styles/
    │   ├── types/
    │   ├── App.tsx
    │   └── main.tsx
    ├── package.json
    ├── vite.config.ts
    └── tsconfig.json
```

---

## ✅ Pré-requisitos

Antes de rodar o projeto, é necessário ter instalado:

- **Java 17** ou superior;
- **Maven**;
- **Node.js**;
- **npm**;
- **PostgreSQL**;
- um editor de código, como VS Code ou IntelliJ.

Para conferir as versões:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
```

---

## ⚙️ Configurando o banco de dados

O backend usa PostgreSQL. Por padrão, a aplicação espera um banco chamado:

```text
disconnect_db
```

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE disconnect_db;
```

Ou, usando terminal:

```bash
createdb disconnect_db
```

Caso esteja no Windows e prefira interface gráfica, também é possível criar o banco pelo **pgAdmin**.

---

## 🔐 Variáveis de ambiente do backend

O backend já possui um arquivo `application.properties` com valores padrão, mas o ideal é usar um arquivo `.env` local dentro da pasta `backend/` para não depender de senha fixa no código.

Crie o arquivo:

```bash
cd backend
```

```bash
# Linux/macOS
cp .env.example .env
```

Se não existir `.env.example`, crie manualmente um arquivo chamado `.env` dentro de `backend/` com o conteúdo abaixo:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/disconnect_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha_do_postgres

APP_PORT=8080
APP_CORS_ALLOWED_ORIGIN=http://localhost:5173
APP_AUTH_SECRET=troque-essa-chave-em-producao

# IA opcional
AI_MOCK_ENABLED=true
# ANTHROPIC_API_KEY=sua_chave_aqui
ANTHROPIC_MODEL=claude-haiku-4-5-20251001
```

> **Importante:** não envie o arquivo `.env` para o GitHub. Ele deve ficar apenas na sua máquina.

---

## 🚀 Rodando o backend

Entre na pasta do backend:

```bash
cd backend
```

Instale/compile as dependências com Maven:

```bash
mvn clean install
```

Execute as migrations do banco com Flyway:

```bash
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/disconnect_db \
  -Dflyway.user=postgres \
  -Dflyway.password=sua_senha_do_postgres
```

No Windows PowerShell, o mesmo comando pode ser escrito assim:

```powershell
mvn flyway:migrate `
  -Dflyway.url=jdbc:postgresql://localhost:5432/disconnect_db `
  -Dflyway.user=postgres `
  -Dflyway.password=sua_senha_do_postgres
```

Depois, inicie a API:

```bash
mvn exec:java
```

Se tudo estiver certo, o backend ficará disponível em:

```text
http://localhost:8080
```

Para testar rapidamente:

```text
http://localhost:8080/health
```

A resposta esperada é algo parecido com:

```json
{
  "status": "ok"
}
```

---

## 🖥️ Rodando o frontend

Em outro terminal, entre na pasta do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm run dev
```

O frontend ficará disponível em:

```text
http://localhost:5173
```

Por padrão, o Vite roda na porta `5173` e o proxy do projeto encaminha chamadas de `/api` para:

```text
http://localhost:8080
```

Também é possível definir a URL da API manualmente criando um arquivo `.env` dentro da pasta `frontend/`:

```env
VITE_API_URL=http://localhost:8080/api
```

---

## 🐳 Rodando o backend com Docker

O backend possui um `Dockerfile`. Para criar a imagem:

```bash
cd backend
docker build -t disconnect-backend .
```

Depois, rode o container apontando para o PostgreSQL:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/disconnect_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=sua_senha_do_postgres \
  -e APP_CORS_ALLOWED_ORIGIN=http://localhost:5173 \
  -e APP_AUTH_SECRET=troque-essa-chave-em-producao \
  disconnect-backend
```

> Em algumas distribuições Linux, `host.docker.internal` pode não funcionar diretamente. Nesse caso, pode ser necessário usar `--add-host=host.docker.internal:host-gateway` ou rodar o PostgreSQL em outro container/rede Docker.

---

## 🔗 Principais rotas da API

### Autenticação

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/login` | Realiza login do usuário |

### Usuários

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/usuarios` | Cadastra um novo usuário |
| `GET` | `/api/usuarios/:id` | Busca usuário por ID |
| `GET` | `/api/usuarios?nome=` | Busca usuários por nome |
| `PUT` | `/api/usuarios/:id` | Atualiza o perfil do usuário autenticado |
| `DELETE` | `/api/usuarios/:id` | Remove o usuário autenticado |

### Eventos

| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/api/categorias` | Lista categorias/modalidades disponíveis |
| `POST` | `/api/eventos?organizadorId=` | Cria um novo evento |
| `GET` | `/api/eventos` | Lista eventos |
| `GET` | `/api/eventos?organizadorId=` | Lista eventos de um organizador |
| `GET` | `/api/eventos/:id` | Busca evento por ID |
| `PUT` | `/api/eventos/:id` | Atualiza um evento |
| `DELETE` | `/api/eventos/:id` | Remove um evento |

### Participações

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/participacoes` | Solicita participação em um evento |
| `GET` | `/api/participacoes?eventoId=` | Lista participações de um evento |
| `GET` | `/api/participacoes?usuarioId=` | Lista participações de um usuário |
| `PUT` | `/api/participacoes/:eventoId/:usuarioId` | Atualiza a mensagem de solicitação |
| `PUT` | `/api/participacoes/:eventoId/:usuarioId/resposta` | Aprova ou recusa uma solicitação |
| `DELETE` | `/api/participacoes/:eventoId/:usuarioId` | Remove uma participação |

### Avaliações

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/avaliacoes` | Cria uma avaliação |
| `GET` | `/api/avaliacoes?eventoId=` | Lista avaliações de um evento |

### IA

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/ia/eventos/recomendacoes` | Gera recomendações de eventos para o usuário autenticado |
| `POST` | `/api/ia/eventos/rascunho` | Gera um rascunho de evento a partir de texto livre |

---

## 🧭 Rotas principais do frontend

| Rota | Página | Acesso |
| --- | --- | --- |
| `/landing` | Página inicial pública | Público |
| `/` | Home com eventos | Autenticado |
| `/events` | Meus eventos | Autenticado |
| `/events/create` | Criar evento | Autenticado |
| `/events/:id` | Detalhes do evento | Autenticado |
| `/events/:id/edit` | Editar evento | Autenticado |
| `/profile` | Meu perfil | Autenticado |
| `/profile/:id` | Perfil de outro usuário | Autenticado |
| `/profile/edit` | Editar perfil | Autenticado |
| `/profile/delete` | Excluir perfil | Autenticado |

---

## ⚠️ Possíveis problemas e soluções

### 1. Erro ao conectar com o PostgreSQL

Verifique se:

- o PostgreSQL está rodando;
- o banco `disconnect_db` existe;
- usuário e senha estão corretos;
- a porta `5432` está disponível;
- o `.env` do backend está com os valores certos.

### 2. Frontend não consegue chamar o backend

Verifique se:

- o backend está rodando em `http://localhost:8080`;
- o frontend está rodando em `http://localhost:5173`;
- o arquivo `vite.config.ts` está usando o proxy para `localhost:8080`;
- a variável `VITE_API_URL`, se usada, aponta para `http://localhost:8080/api`.

### 3. Erro de CORS

O backend permite por padrão a origem:

```text
http://localhost:5173
```

Se o frontend estiver em outra porta ou domínio, altere:

```env
APP_CORS_ALLOWED_ORIGIN=http://localhost:5173
```

### 4. Recurso de IA não funciona

A IA é opcional. Para desenvolvimento local, é possível deixar:

```env
AI_MOCK_ENABLED=true
```

Caso queira usar a integração real com Anthropic/Claude, configure:

```env
ANTHROPIC_API_KEY=sua_chave_aqui
```

### 5. Porta já em uso

Se a porta `8080` ou `5173` estiver ocupada, encerre o processo que está usando a porta ou altere a configuração correspondente.

---

## 🧪 Scripts úteis

### Backend

```bash
mvn clean install
mvn flyway:migrate
mvn exec:java
```

### Frontend

```bash
npm install
npm run dev
npm run build
npm run lint
npm run preview
```

---

## 📌 Status do projeto

Este projeto foi desenvolvido inicialmente como trabalho acadêmico e depois migrado para este repositório pessoal. Ainda pode receber melhorias, ajustes de arquitetura, documentação e refinamentos na interface.

Alguns pontos que podem ser evoluídos futuramente:

- adicionar testes automatizados;
- criar um `docker-compose.yml` para subir backend, frontend e banco juntos;
- melhorar o controle de autenticação/autorização;
- adicionar paginação e filtros mais avançados;
- melhorar validações no backend;
- publicar uma versão em produção.

---

## 👨‍💻 Autor

Desenvolvido por **Felipe Silva**.

GitHub: [@FelipeSilva96](https://github.com/FelipeSilva96)

---

## 📄 Licença

Este projeto ainda não possui uma licença definida.
