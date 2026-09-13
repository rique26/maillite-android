# MailLite Android

App Android do **MailLite**, um MVP de correio eletrônico minimalista. Consome a [MailLite API](#) para cadastro/login, busca de destinatários, envio e leitura de mensagens, e recebe notificações push em tempo real via Firebase Cloud Messaging.

> Este app é o par do backend MailLite (Spring Boot) e é o foco principal de avaliação do projeto — a API existe para sustentar esta demonstração de forma simples e direta.

---

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Android Views + View Binding + Material Components |
| Arquitetura | Clean Architecture + MVVM |
| Injeção de dependência | Hilt |
| Navegação | Navigation Component (Safe Args) |
| Assincronismo | Coroutines + Flow/StateFlow |
| Rede | Retrofit + OkHttp (logging interceptor) |
| Persistência local | DataStore Preferences (token JWT) |
| Notificações push | Firebase Cloud Messaging |
| Build | Gradle (Kotlin DSL), compileSdk/targetSdk 36, minSdk 24 |
| Testes | JUnit + MockK + kotlinx-coroutines-test |

---

## Arquitetura

O projeto segue **Clean Architecture** organizada por **pacote por feature** (package-by-feature), com três camadas por feature — `data`, `domain` e `presentation` — favorecendo alta coesão e testabilidade.

```
com.rique.maillite
├── core/                          # Infraestrutura e utilitários compartilhados
│   ├── data/
│   │   ├── local/                 # TokenDataStore (JWT persistido)
│   │   ├── mapper/                # Mapeamento de paginação (PageMapper)
│   │   └── remote/                # AuthInterceptor, SafeApiCall, DTOs de erro/paginação
│   ├── di/                        # Módulos Hilt (Network, Repository)
│   ├── domain/
│   │   ├── model/                 # PagedResult
│   │   └── util/                  # Result (wrapper de sucesso/erro)
│   ├── extensions/                 # Extensões de View (insets, etc.)
│   ├── push/                      # FcmTokenProvider, FcmTokenSynchronizer, MailLiteFirebaseMessagingService
│   └── util/                      # JwtUtil, AvatarUtil
│
├── features/
│   ├── splash/presentation/       # Verifica sessão ativa e decide o destino inicial
│   ├── auth/                      # Login e registro (RF01, RF02)
│   │   ├── data/                  # AuthApiService, DTOs, AuthRepositoryImpl
│   │   ├── domain/                # AuthRepository, LoginUseCase, RegisterUseCase, LogoutUseCase, CheckSessionUseCase
│   │   └── presentation/          # LoginFragment/ViewModel, RegisterFragment/ViewModel
│   │
│   ├── users/                     # Busca de destinatários e token FCM (RF03, RF08)
│   │   ├── data/                  # UserApiService, DTOs, UserRepositoryImpl
│   │   └── domain/                # UserRepository, SearchUsersUseCase, UpdateFcmTokenUseCase
│   │
│   └── messages/                  # Inbox, envio, leitura, exclusão (RF04-RF07)
│       ├── data/                  # MessageApiService, DTOs, MessageRepositoryImpl
│       ├── domain/                # MessageRepository, GetInboxUseCase, SendMessageUseCase, GetMessageByIdUseCase, DeleteMessageUseCase
│       └── presentation/
│           ├── inbox/             # InboxFragment/ViewModel — lista paginada, swipe-to-delete com undo, logout
│           ├── compose/           # ComposeMessageFragment/ViewModel — autocomplete de destinatário
│           └── detail/            # MessageDetailFragment/ViewModel — marca como lida automaticamente
│
├── MailLiteApplication.kt
└── MainActivity.kt                # Host de navegação (NavHostFragment + Toolbar)
```

### Fluxo de navegação

```
Splash ──(sessão válida)──> Inbox
  └──(sem sessão)──────────> Login ──> Registro
                               └──> Inbox ──> Detalhe da Mensagem
                                      └──> Nova Mensagem
```

- `SplashFragment` decide a tela inicial verificando se há um token JWT válido persistido (`CheckSessionUseCase`).
- Da Inbox, o logout limpa a sessão e retorna à tela de Login, removendo toda a back stack (`popUpTo`/`popUpToInclusive`).

### Autenticação e sessão

- O JWT retornado no login é persistido via **DataStore Preferences** (`TokenDataStore`).
- `AuthInterceptor` injeta automaticamente o header `Authorization: Bearer <token>` em toda requisição autenticada.
- `JwtUtil` extrai informações do token localmente (ex.: validade) sem chamada de rede.
- Sessão expira junto com o token (24h, sem refresh token) — o usuário refaz login normalmente.

### Rede e tratamento de erros

- Toda chamada de API passa por `SafeApiCall`, que encapsula sucesso/erro em um `Result` de domínio, tratando falhas HTTP e de conexão de forma padronizada.
- Erros do backend seguem o formato `{ "message": ..., "status": ... }`, mapeados para mensagens exibidas ao usuário (Snackbar).
- Paginação da inbox (`GET /messages/inbox`) é mapeada de `PageResponseDto` para `PagedResult`, com scroll infinito (carrega a próxima página ao chegar perto do fim da lista).

### Notificações push

- `MailLiteFirebaseMessagingService` recebe as notificações de novas mensagens enviadas pelo backend.
- `FcmTokenProvider`/`FcmTokenSynchronizer` obtêm o token FCM do dispositivo e o sincronizam com o backend (`POST /users/fcm-token`) após login e a cada renovação de token.
- Permissão de notificação (`POST_NOTIFICATIONS`, Android 13+) é solicitada em runtime na `MainActivity`.

---

## Telas

| Tela | Fragment | Descrição |
|---|---|---|
| Splash | `SplashFragment` | Verifica sessão ativa e redireciona para Login ou Inbox |
| Login | `LoginFragment` | Autenticação por e-mail e senha |
| Registro | `RegisterFragment` | Cadastro de novo usuário |
| Inbox | `InboxFragment` | Lista paginada de mensagens recebidas, com indicador lido/não lido, swipe-to-delete com desfazer e scroll infinito |
| Nova Mensagem | `ComposeMessageFragment` | Composição de mensagem com autocomplete de destinatário |
| Detalhe da Mensagem | `MessageDetailFragment` | Leitura da mensagem (marcada como lida automaticamente) |

---

## Como rodar localmente

### Pré-requisitos

- Android Studio (Ladybug ou superior)
- JDK 17
- Um dispositivo/emulador com Android 7.0 (API 24) ou superior

### 1. Backend

O app consome a [MailLite API](#) hospedada em produção (`https://maillite-api.onrender.com`), configurada em `NetworkModule.kt`. Para apontar para uma instância local do backend, altere a constante `BASE_URL` nesse arquivo antes de compilar.

### 2. Firebase (notificações push)

O projeto já inclui um `google-services.json` de exemplo. Para usar um projeto Firebase próprio, substitua o arquivo em `app/google-services.json` pelo baixado no Console do Firebase (Configurações do projeto → Seus apps → Android).

### 3. Compilar e rodar

Via Android Studio: abra o projeto e rode a configuração padrão `app` em um emulador ou dispositivo físico.

Via linha de comando:

```bash
./gradlew installDebug
```

---

## Decisões de escopo (MVP)

Escolhas conscientes para manter o app simples, já que o foco de avaliação do teste técnico é a demonstração do fluxo completo:

- **Sem cache local (Room) das mensagens**: a dependência está no projeto, mas a inbox depende diretamente da API — sem persistência offline neste MVP.
- **Sem múltiplas contas**: um único usuário logado por vez.
- **Sem anexos**: mensagens são apenas texto (assunto + corpo).
- **Sem refresh token**: alinhado ao backend, o token expira em 24h e o usuário refaz login.