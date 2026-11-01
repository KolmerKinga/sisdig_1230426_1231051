# auth-service — Identity & Access

Serviço que **autentica os colaboradores e emite os tokens JWT** usados por todos os serviços do AISafe. Corresponde ao `authUsers` e `Users` do PSOFT, separado num subdomínio genérico (*Identity & Access*).

É um projeto Spring Boot **independente** e não chama nenhum outro serviço.

## Como funciona

```
cliente ──POST /auth/login {username, password}──► auth-service (8051 ou 8052)
                                                   │ BCrypt compara a password
                                                   │ assina o JWT com AISAFE_JWT_SECRET
cliente ◄──── {token, tokenType, expiresIn, username, roles} ┘

cliente ──Authorization: Bearer <token>──► airport / maintenance / ...
                                           │ cada serviço valida o token LOCALMENTE (mesma chave)
                                           │ e aplica o RBAC com os roles do token
```

- **Os outros serviços nunca chamam o auth-service.** Validam a assinatura do token com a chave partilhada. Se o auth-service cair, os tokens já emitidos continuam válidos até expirarem (1 hora). Só os logins novos ficam indisponíveis.
- **Duas réplicas com os mesmos utilizadores.** Os colaboradores são dados de referência replicados: as duas réplicas carregam o mesmo `seed/collaborators.json`. Por isso qualquer réplica faz login e não há forwarding. O id de cada colaborador é derivado do username, por isso o `sub` do token é igual, seja qual for a réplica.
- **Contrato do token** (é o mesmo que os outros serviços já esperam):

  | Claim | Conteúdo |
  |---|---|
  | `sub` | id do colaborador |
  | `username` | nome de login (aparece nos logs de auditoria) |
  | `roles` | `ADMIN`, `ATCC`, `BACKOFFICE`, `MAINTENANCE_TECHNICIAN`, `MAINTENANCE_SUPERVISOR` |
  | `iss`, `iat`, `exp` | emissor, data de emissão e expiração (1 h) |

  Assinatura HMAC-SHA256.

## Endpoints

| Método e caminho | Acesso | Resposta |
|---|---|---|
| `POST /auth/login` | público | `200` com o token; `401` credenciais erradas; `429` utilizador bloqueado |
| `GET /auth/me` | token válido | identidade contida no token e réplica que o validou |
| `GET /actuator/health` | público | estado da réplica |

## Utilizadores (iguais ao bootstrap do PSOFT)

| Username | Password | Papel |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `atcc` | `atcc123` | ATCC |
| `backoffice` | `backoffice123` | BACKOFFICE |
| `technician` | `technician123` | MAINTENANCE_TECHNICIAN |
| `supervisor` | `supervisor123` | MAINTENANCE_SUPERVISOR |
| `lockout-demo` | `lockout123` | ATCC (só para demonstrar o bloqueio) |

As passwords do seed são de desenvolvimento e são guardadas com hash BCrypt, nunca em texto.

## Segurança

- HTTPS em todas as portas (mesmo certificado dos outros serviços).
- Passwords com BCrypt.
- "Utilizador desconhecido" e "password errada" dão a mesma resposta (401), e o tempo de resposta também é igual. Assim não se descobre que usernames existem.
- **Proteção contra força bruta:** ao fim de 5 passwords erradas, o username fica bloqueado 5 minutos nessa réplica (`429` com `Retry-After`).
- Log de auditoria (`logs/audit-auth-N.log`) com `LOGIN_SUCCESS`, `LOGIN_FAILED` e `LOGIN_BLOCKED`. A password nunca é escrita.

## Como correr

Dentro de `AISafe/auth-service` (o certificado tem de existir em `AISafe/certs`):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=instance1   # https://localhost:8051
mvn spring-boot:run -Dspring-boot.run.profiles=instance2   # https://localhost:8052
```

Teste rápido:
```bash
curl -k -X POST https://localhost:8051/auth/login -H "Content-Type: application/json" \
     -d '{"username":"atcc","password":"atcc123"}'
```

Postman: `AISafe/tests/postman/auth-service.postman_collection.json`.

## Variáveis de ambiente

| Variável | Valor por omissão (só para desenvolvimento) |
|---|---|
| `AISAFE_JWT_SECRET` | segredo de desenvolvimento (Base64); **tem de ser igual em todos os serviços** |
| `AISAFE_SERVICE_KEY` | `dev-service-key-change-me` |
| `AISAFE_CERTS_DIR` | `../certs` |
| `AISAFE_KEYSTORE_PASSWORD` | `changeit` |

## Limitações conhecidas

- O bloqueio por força bruta é guardado em memória por réplica: um atacante que alterne entre as duas réplicas tem o dobro das tentativas.
- Não há gestão de utilizadores pela API (criar ou alterar), tal como no PSOFT. Os utilizadores vêm do seed.
