# maintenance-service — Maintenance Management

Serviço do contexto **Maintenance Management** do AISafe distribuído (SIDIS, Assignment 1). É dono dos **templates de manutenção** e dos **registos de manutenção** (PSOFT US115, US116, US117, US119, US217–US222, US226).

É um projeto Spring Boot **independente**. Chama **um** serviço: o `aircraft-service` (Customer/Supplier). É chamado pelo `route-flight-service` (`GET /api/maintenance/aircraft/{reg}/ongoing`).

## Réplicas e dados

| Réplica | Perfil | Porta | Peer | Base de dados | Dados iniciais |
|---|---|---|---|---|---|
| maintenance-1 | `instance1` | 8061 | 8062 | `jdbc:h2:mem:maintenance_8061` | templates A-Check e Engine Borescope; registos do CS-TMA |
| maintenance-2 | `instance2` | 8062 | 8061 | `jdbc:h2:mem:maintenance_8062` | templates C-Check e Avionics Update; registos do CS-TMB |

Os dados estão **particionados**: cada template e cada registo vive na réplica que o criou, com id **UUID** (não há colisões entre réplicas). A peça `FLT-OIL-01` foi usada nas duas réplicas (6 + 5 unidades), por isso o alerta de stock baixo (limite 10) só aparece quando os dados são agregados.

## Endpoints

| Método e caminho | US | Papéis | Distribuição |
|---|---|---|---|
| `POST /api/maintenance/templates` | 115 | TECHNICIAN, ADMIN | Guarda nesta réplica |
| `GET /api/maintenance/templates` | 115 | TECHNICIAN, SUPERVISOR, ADMIN | Agrega as duas réplicas |
| `GET /api/maintenance/templates/{id}` | 115 | TECHNICIAN, SUPERVISOR, ADMIN | Local; se não existir, pergunta ao peer |
| `POST /api/maintenance/records` (e `/record`, como no PSOFT) | 115 | TECHNICIAN, ADMIN | Guarda nesta réplica; o template pode estar no peer; a aeronave é validada no aircraft-service |
| `GET /api/maintenance/records?aircraft=&attribute=&status=&initialDate=&finalDate=&page=&size=` | 116, 218 | TECHNICIAN, SUPERVISOR, ADMIN | Agrega, ordena e pagina |
| `GET /api/maintenance/records/{id}` | 119 | TECHNICIAN, SUPERVISOR, ADMIN | Local; se não existir, pergunta ao peer |
| `PATCH /api/maintenance/records/{id}/status` `{"status":"ONGOING"}` | 119 | TECHNICIAN, SUPERVISOR, ADMIN | Na réplica dona; aeronave passa a `MAINTENANCE` |
| `POST /api/maintenance/records/{id}/complete` | 119 | TECHNICIAN, SUPERVISOR, ADMIN | Na réplica dona; aeronave volta a `AVAILABLE` |
| `POST /api/maintenance/records/{id}/parts` | 220, 226 | TECHNICIAN, SUPERVISOR, ADMIN | Na réplica dona |
| `PATCH /api/maintenance/records/{id}` `{"attribute":"ENGINE"}` | 217 | TECHNICIAN, SUPERVISOR, ADMIN | Na réplica dona |
| `GET /api/maintenance/ongoing` | 219 | TECHNICIAN, SUPERVISOR, ADMIN | Agregado |
| `GET /api/maintenance/turnaround-average` | 221 | SUPERVISOR, ADMIN | Agregado |
| `GET /api/maintenance/reports/costs?groupBy=aircraft\|model` | 220 | ATCC, SUPERVISOR, ADMIN | Agregado |
| `GET /api/maintenance/reports/hours` | 117 | ATCC, SUPERVISOR, ADMIN | Agregado |
| `GET /api/maintenance/alerts?calendarDaysThreshold=&flightHoursThreshold=` | 222 | ATCC, SUPERVISOR, ADMIN | Agregado + frota do aircraft-service |
| `GET /api/maintenance/parts/inventory` | 226 | SUPERVISOR, ADMIN | Agregado |
| `GET /api/maintenance/parts/low-stock?threshold=` | 226 | SUPERVISOR, ADMIN | Agregado |
| `GET /api/maintenance/aircraft/{reg}/ongoing` | — | ATCC, TECHNICIAN, SUPERVISOR, ADMIN | Contrato para o route-flight-service |
| `GET /api/maintenance/internal/**` | — | **só réplicas** (chave de serviço) | Devolve só os dados locais |

**Ciclo de vida de um registo:** `INLINE` (planeado) → `ONGOING` → `COMPLETED`. Para concluir é preciso marcar como feitas **todas as tarefas obrigatórias** do checklist (caso contrário dá 409), e a data de fim não pode ser anterior à de início (400).

## Distribuição

- **Um recurso** (template, registo e comandos sobre um registo): procura primeiro no H2 local. Se não estiver, reenvia o mesmo pedido ao peer com `X-Forwarded-By` (no máximo 1 salto) e devolve a resposta de quem o tem.
- **Coleções e relatórios:** a réplica junta os seus registos com os do peer, lidos no endpoint interno `/api/maintenance/internal/records`. Os relatórios são calculados sobre essa lista. Por isso:
  - o resultado é o mesmo em qualquer réplica;
  - um utilizador com o papel ATCC pode pedir um relatório, mesmo não tendo acesso direto à lista de registos do peer.
- **Endpoints internos:** só aceitam chamadas com `X-Service-Key` válida, nunca reencaminham e só devolvem dados locais. Assim não há cadeias de chamadas.
- **Tolerância a falhas** (igual aos outros serviços):
  - timeouts de 1 s e 2 s;
  - 2 tentativas com backoff;
  - circuit breaker de 10 s;
  - `X-Partial-Result: true` quando um peer não respondeu.

## Integração com o aircraft-service

- `GET /api/aircraft/{reg}`: ao criar um registo, confirma que a aeronave existe e copia o modelo, que é usado nos relatórios por modelo.
- `PATCH /api/aircraft/{reg}/status`: põe a aeronave em `MAINTENANCE` quando o trabalho começa, e de volta em `AVAILABLE` quando acaba. Só a liberta se **nenhuma** réplica tiver outra manutenção em curso para ela. Se não conseguir confirmar isso com todas as réplicas, não a liberta (segurança primeiro).
- `GET /api/aircraft`: devolve a frota, usada nos alertas (US222).
- O cliente conhece as **duas réplicas** do aircraft-service (`aisafe.services.aircraft`) e passa à seguinte se uma não responder.
- Se o aircraft-service estiver em baixo:
  - criar um registo dá **503** (a aeronave não pode ser validada);
  - iniciar ou concluir um registo **continua a funcionar**, porque a manutenção é dona dos seus dados. A resposta traz `aircraftSync: "FAILED: ..."` para mostrar que o estado da aeronave ficou por atualizar.

## Segurança

- HTTPS em todas as portas.
- JWT do `auth-service`, validado localmente com a chave partilhada.
- RBAC por endpoint (tabela acima).
- `X-Service-Key` obrigatória nas chamadas internas.
- Auditoria em `logs/audit-maintenance-N.log`.
- O utilizador que cria e o que conclui cada registo ficam guardados (`createdBy` e `completedBy`, a partir do token).

## Como correr

Dentro de `AISafe/maintenance-service` (com o certificado em `AISafe/certs`):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=instance1   # https://localhost:8061
mvn spring-boot:run -Dspring-boot.run.profiles=instance2   # https://localhost:8062
```
Para os testes completos, o `auth-service` (login) e o `aircraft-service` (8081) também têm de estar a correr.

Postman: `AISafe/tests/postman/maintenance-service.postman_collection.json`.

## Variáveis de ambiente

| Variável | Valor por omissão |
|---|---|
| `AISAFE_SERVICES_AIRCRAFT` | `http://localhost:8081,http://localhost:8082` |
| `AISAFE_JWT_SECRET` | segredo de desenvolvimento; **igual em todos os serviços** |
| `AISAFE_SERVICE_KEY` | `dev-service-key-change-me` |
| `AISAFE_CERTS_DIR` | `../certs` |
| `AISAFE_PEERS`, `AISAFE_INSTANCEID`, `SERVER_PORT` | definidos pelo perfil `instanceN` |

## Diferenças face ao PSOFT

- A aeronave e o técnico passam a ser identificados pela matrícula e pelo username, em vez de relações JPA para `Aircraft` e `Collaborator`, que pertencem a outros serviços.
- O registo guarda uma **cópia** dos dados do template (nome, tipo, componente, checklist), porque o template pode estar noutra réplica. Mudar o componente de um registo (US217) já não altera o template partilhado, ao contrário do que acontecia no PSOFT.
- Foram acrescentados os endpoints que faltavam no PSOFT:
  - concluir um registo (`/complete`, US119);
  - registar peças (`/parts`), sem o qual os relatórios de custos e de inventário ficavam sempre vazios;
  - horas de manutenção da frota (`/reports/hours`, US117).

## Limitações conhecidas

- O H2 é em memória: uma réplica que reinicie perde o que foi criado nela (o seed volta a ser carregado).
- Se o aircraft-service estiver em baixo quando um registo é iniciado ou concluído, o estado da aeronave fica desatualizado até nova operação (consistência eventual). O campo `aircraftSync` mostra quando isso acontece.
