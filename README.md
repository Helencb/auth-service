# auth-service

Microsserviço de autenticação e autorização (Java 21 / Spring Boot 3.5), pensado para
rodar dentro de uma arquitetura de microsserviços com Eureka/API Gateway.

## Funcionalidades

- **Cadastro e login** (`/auth`) com emissão de access token + refresh token (JWT)
- **MFA/TOTP** (`/mfa`) compatível com Google Authenticator, com QR Code
- **Sessões** (`/sessions`) — listar/revogar sessões ativas por dispositivo
- **Reset de senha** (`/password`) — fluxo "esqueci minha senha" por email com token de uso único
- **Login social (OAuth2)** (`/oauth2`) — login via Google, provisionando o usuário local e
  emitindo os mesmos JWTs do login tradicional
- **Health check** (`/health`) + Actuator/Prometheus

## Stack

- Spring Boot 3.5 + Spring Cloud (Eureka Client)
- Spring Security + JWT (jjwt 0.12) + OAuth2 Client
- Spring Data JPA + MySQL, migrações via Flyway
- Redis (cache/sessão), RabbitMQ, AWS SQS
- MFA/TOTP via `googleauth` + QR Code via `zxing`
- springdoc-openapi (Swagger UI)

## Rodando localmente

Pré-requisitos: MySQL, Redis e (opcionalmente) Eureka Server e RabbitMQ rodando localmente
com as portas padrão (veja `application.yml`).

```bash
./mvnw spring-boot:run
```

A aplicação sobe na porta **8081**. Perfis disponíveis: `dev`, `prod`
(`--spring.profiles.active=dev`).

- Swagger UI: http://localhost:8081/swagger-ui.html
- Health check: http://localhost:8081/health

## Testes

```bash
./mvnw test
```

Os testes são unitários (JUnit 5 + Mockito), sem dependência de banco/Redis reais.

## Configuração

Variáveis de ambiente relevantes (todas com fallback de desenvolvimento em `application.yml`):

| Variável | Descrição |
|---|---|
| `JWT_SECRET` | Chave usada para assinar os JWTs. **Defina em produção** — o valor padrão é apenas para dev. |
| `MAIL_ENABLED` | `true` para efetivamente enviar emails (reset de senha, aviso de MFA); `false` (padrão) apenas loga o conteúdo. |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Credenciais SMTP. |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Credenciais OAuth2 do Google, necessárias para o login social funcionar. |
| `FRONTEND_URL` | Usada para montar o link de reset de senha enviado por email. |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas por CORS (separadas por vírgula). |

## Endpoints principais

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/auth/register` | pública | Cadastro de usuário |
| POST | `/auth/login` | pública | Login (retorna `mfaRequired=true` se MFA estiver ativo) |
| POST | `/auth/refresh` | pública | Renova o access token |
| POST | `/mfa/setup` | JWT | Gera segredo TOTP + QR Code |
| POST | `/mfa/enable` | JWT | Confirma e ativa o MFA |
| POST | `/mfa/verify` | pública | Valida um código TOTP |
| GET | `/sessions` | JWT | Lista sessões ativas |
| DELETE | `/sessions/{id}` | JWT | Revoga uma sessão |
| POST | `/password/forgot` | pública | Solicita reset de senha por email |
| POST | `/password/reset` | pública | Efetiva o reset com o token recebido |
| GET | `/oauth2/providers` | pública | Lista provedores de login social disponíveis |
| GET | `/oauth2/success` | pós-login OAuth2 | Retorna os JWTs após login social bem-sucedido |

## Integração com o api-gateway (github.com/Helencb/api_gateway)

Este serviço registra-se no Eureka (`spring.application.name: auth-service` → aparece como
`AUTH-SERVICE`) e o gateway já tem uma rota `lb://AUTH-SERVICE` configurada para `/api/auth/**`.
Para o par funcionar de ponta a ponta, dois contratos precisam ficar sincronizados:

- **Segredo do JWT**: o gateway valida a assinatura com sua própria propriedade
  `security.jwt.secret` (não busca isso do auth-service). O valor padrão aqui foi ajustado para
  bater com o default atual do gateway (`my-super-secret-key-my-super-secret-key`) — funciona
  "de fábrica", mas em qualquer ambiente real defina `JWT_SECRET` (aqui) e `security.jwt.secret`
  (no gateway) com o **mesmo valor**.
- **Claim de role**: o `AuthorizationFilter` do gateway lê um claim `role` (string única, sem
  prefixo `ROLE_`, ex: `"USER"`/`"ADMIN"`) do header `X-User-Role`. Adicionei esse claim em
  `JwtClaimsFactory` especificamente para isso (mantendo o claim `roles` em lista, que já
  existia, para outros usos).

**Ainda não resolvido — precisa de uma decisão sua, porque toca o outro repositório:**

- O gateway roteia `/api/auth/**` para este serviço **sem remover o prefixo** (`StripPrefix`),
  mas os controllers aqui respondem em `/auth/**`, `/mfa/**`, `/sessions/**`, `/password/**` e
  `/oauth2/**` (sem `/api`). Ou seja, hoje uma chamada via gateway pra `/api/auth/login` bateria
  em `/api/auth/login` aqui, que não existe (404).
- Além disso, o gateway só tem rota configurada para `/api/auth/**` — MFA, sessões, reset de
  senha e OAuth2 não têm rota nenhuma no gateway ainda, então não são alcançáveis por ele.

  Duas formas de resolver (me diga qual prefere, ou se quer as duas):
  1. Neste repositório: definir `server.servlet.context-path: /api` — todos os controllers
     passam a responder em `/api/auth/**`, `/api/mfa/**` etc., alinhando com a rota já existente
     no gateway para `/auth`; ainda faltaria o gateway ganhar rotas novas para mfa/sessions/password/oauth2.
  2. No `api_gateway`: adicionar um filtro `StripPrefix=1` na rota `auth-service` e novas rotas
     para `/api/mfa/**`, `/api/sessions/**`, `/api/password/**`, `/api/oauth2/**` apontando pra
     `lb://AUTH-SERVICE`.

## Limitações conhecidas / débito técnico

- Vários componentes de infraestrutura (`resilience/*`, `observability/*`, `mapper/*`,
  `integration/aws/*`, `config/GatewaySecurityConfig`, `config/WebMvcConfig`,
  `config/OpenApiConfig`/`SwaggerConfig`) ainda são classes vazias — não têm um contrato de
  uso definido no código-base e foram deixadas de fora desta rodada para não inventar
  comportamento sem especificação.
- Os repositórios JPA declaram `JpaRepository<Entity, String>` mas os IDs das entidades são
  `UUID` — funciona hoje porque só usam finders customizados, mas `SessionService.revokeSession`
  chama `findById(String)` diretamente, o que é uma inconsistência de tipos a ser corrigida.
- O login social provisiona o usuário com uma senha aleatória (nunca exposta) — não há como
  esses usuários fazerem login por senha depois; se for necessário permitir "linkar" contas,
  isso precisa de um fluxo à parte.
