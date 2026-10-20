# Justificação da arquitetura segundo DDD

O AISafe distribuído está organizado em **quatro Bounded Contexts de negócio** e num **subdomínio genérico de identidade**, seguindo os princípios de **Domain-Driven Design (DDD)**. Um Bounded Context delimita uma área de negócio com responsabilidades, regras e dados próprios. Cada contexto é implementado como um serviço Spring Boot independente, com duas réplicas.

O código PSOFT (monólito) mantém-se inalterado na raiz do repositório e serve apenas de referência. Toda a implementação de SIDIS está em `AISafe/`.

## Critério de divisão

Os contextos foram definidos pelas responsabilidades de negócio e pela proximidade entre os conceitos do domínio. Conceitos que participam nas mesmas operações e regras ficaram no mesmo contexto.

Por exemplo, os modelos e as aeronaves pertencem à gestão da frota, enquanto os registos e os templates de manutenção pertencem à gestão das intervenções. As rotas e os voos ficaram juntos porque o agendamento de um voo usa uma rota e atualiza o seu histórico: no PSOFT, `RouteService.scheduleFlight()` cria o `Flight` e incrementa a utilização da rota na mesma transação. Mantê-los no mesmo serviço permite que esta operação continue a ser uma transação local, sem coordenação distribuída.

O enunciado de SIDIS propõe três serviços (Aircraft & Maintenance, Airports & Routes, Flight Operations), adaptáveis à dimensão do grupo. A proposta usa quatro serviços de negócio, separando Aircraft de Maintenance, porque têm ciclos de vida, regras e utilizadores distintos (ATCC vs. técnicos de manutenção). O grupo ter quatro elementos facilita a distribuição do trabalho, mas o critério principal é a separação das responsabilidades de negócio.

## Bounded Contexts

| Contexto | Serviço | Tipo | Responsabilidade | Principais conceitos |
| --- | --- | --- | --- | --- |
| **Aircraft Management** | `aircraft-service` | core | Modelos, aeronaves, configuração, componentes, certificações e estado operacional da frota. | `AircraftModel`, `Aircraft`, `AircraftSpecs`, `SeatingPack`, `InstalledComponent`, `AircraftCertification`, `AircraftAvailability` |
| **Airport Management** | `airport-service` | supporting | Aeroportos, pistas, instalações, contactos, estado operacional e certificações do aeroporto. | `Airport`, `Runway`, `Facilities`, `AirportStatus`, `Certification` |
| **Route / Flight Operations** | `route-flight-service` | core | Rotas, histórico de rotas e voos agendados; coordena as verificações entre domínios no agendamento. | `Route`, `RouteHistory`, `RouteRequirements`, `Flight` |
| **Maintenance Management** | `maintenance-service` | core | Templates de manutenção, registos de intervenções, progresso, peças e alertas. | `MaintenanceTemplate`, `MaintenanceRecord`, `UsedPart`, `DoneList` |
| **Identity & Access** | `auth-service` | generic | Colaboradores, papéis e emissão de tokens JWT. | `Collaborator`, `Role` |

`AircraftAvailability` é um enum, não uma entidade autónoma. `AircraftCertification` (certificação da aeronave) e `Certification` (certificação do aeroporto) são classes distintas e pertencem a contextos diferentes. No código PSOFT, o voo agendado é a classe `Flight`.

**Identity & Access** é um subdomínio genérico: não tem regras de negócio de aviação, mas no monólito estava misturado com os restantes domínios (por exemplo, `MaintenanceRecord` tinha relações `@ManyToMany` com `Collaborator`). Isolá-lo evita colocar utilizadores num contexto de negócio. Os outros contextos apenas validam o JWT localmente, por isso não dependem do `auth-service` em cada pedido.

## Relações entre contextos

As setas vão do contexto consumidor (*downstream*) para o fornecedor (*upstream*). Todas as relações são **Customer / Supplier** sobre **HTTPS/REST**, e o grafo de dependências é **acíclico**.

| # | Consumidor → Fornecedor | Para quê |
| --- | --- | --- |
| 1 | Route / Flight Operations → Airport Management | Validar origem e destino de rotas e voos: existência, estado operacional, certificações. |
| 2 | Route / Flight Operations → Maintenance Management | Saber se a aeronave tem uma manutenção em curso antes de agendar um voo. |
| 3 | Route / Flight Operations → Aircraft Management | Obter estado, alcance e capacidade da aeronave; registar horas de voo. |
| 4 | Maintenance Management → Aircraft Management | Obter dados da aeronave (modelo, horas de voo); mudar a disponibilidade para `MAINTENANCE` / `AVAILABLE` no início e fim de uma intervenção. |

`aircraft-service` e `airport-service` **não chamam nenhum outro serviço**. No PSOFT existiam consultas no sentido inverso (Aircraft → Route e Airport → Route). Estas consultas passam para o contexto que é dono dos dados de que precisam:

| Consulta PSOFT | Antes | Passa a |
| --- | --- | --- |
| Rotas compatíveis com uma aeronave (US203) | `GET /api/hangar/aircraft/{reg}/compatible-routes` | `GET /api/routes/compatible?aircraft={reg}` em `route-flight-service`, que pede alcance e capacidade a `aircraft-service` |
| Rotas de um aeroporto (US209) | `GET /api/airports/{iata}/routes` | `GET /api/routes?airport={iata}` em `route-flight-service` |
| Aeroportos com mais rotas (US210) | `GET /api/airports/busiest` | `GET /api/routes/stats/busiest-airports` em `route-flight-service` |
| Estatísticas que dependem de voos (US204, US227) | em `HangarController` / `ModelCatalogController` | em `route-flight-service`, que tem os voos |

Assim evita-se acoplamento circular e chamadas em cadeia entre serviços.

## Contratos entre contextos

| Chamada | Estado |
| --- | --- |
| `GET /api/hangar/aircraft/{registrationNumber}` | Existe no PSOFT; a resposta passa a incluir `maximumRangeKm` e `seatingCapacity`, necessários para validar alcance e capacidade. |
| `PATCH /api/hangar/aircraft/{registrationNumber}` (disponibilidade) | Existe no PSOFT (US105). |
| `PATCH /api/hangar/aircraft/{registrationNumber}/flight-hours` | Novo. |
| `GET /api/airports/{iataCode}` | Existe no PSOFT. |
| `GET /api/maintenance/aircraft/{registrationNumber}/ongoing` | Novo; devolve se existe um `MaintenanceRecord` com estado `ONGOING`. |
| `GET /api/flights/aircraft/{registrationNumber}` | Existe no PSOFT; usado também entre réplicas para verificar sobreposição de voos. |

## Propriedade dos dados e comunicação

- Cada contexto é dono da sua lógica de negócio e dos seus dados. **Não existe base de dados partilhada** nem acesso aos repositories de outro contexto.
- As referências entre contextos usam **apenas identificadores**: matrícula da aeronave, código IATA, `routeId`, `username`. As relações JPA que atravessavam contextos no PSOFT (`Flight → Aircraft`, `Route → Airport`, `MaintenanceRecord → Aircraft`, `MaintenanceRecord → Collaborator`) são substituídas por estes identificadores.
- Cada contexto consumidor traduz o JSON recebido para **DTOs próprios** no pacote `client/` (camada anticorrupção). Nenhum serviço importa classes de outro serviço.
- Consultar informação de outro contexto não transfere a propriedade desses dados. Por exemplo, Route / Flight Operations lê o estado da aeronave, mas só Aircraft Management o altera.

## Decisões de distribuição

Estas decisões complementam os Bounded Contexts e estão detalhadas nos diagramas de deployment e de forwarding.

- **Réplicas:** 2 por serviço, cada uma com processo, porta e H2 próprios (portas 805x, 808x, 809x, 807x, 806x).
- **Particionamento:** um registo vive na réplica que o criou. Os IDs gerados passam a **UUID**, para não colidirem entre réplicas. As chaves naturais (matrícula, IATA, nome do modelo) são verificadas nos peers antes da criação (409 se já existirem).
- **Forwarding:** procura primeiro no armazenamento local. Se o dado faltar, consulta o peer com `X-Forwarded-By`, e um pedido reencaminhado nunca volta a ser reencaminhado (no máximo 1 salto). As coleções são agregadas a partir de todas as réplicas, sem duplicados.
- **Tolerância a falhas:**
  - timeouts de 1 s para ligar e 2 s para ler;
  - 2 tentativas com 200 ms de *backoff*;
  - circuit breaker de 10 s por peer;
  - *failover* para a segunda réplica nas chamadas entre serviços;
  - respostas parciais assinaladas com `X-Partial-Result: true`.
- **Consistência:** o sistema privilegia disponibilidade e tolerância a partições (AP), com consistência eventual nas leituras. A exceção é a criação de voos: exige respostas completas e é recusada com 503 se uma verificação não puder ser feita.
- **Segurança:**
  - HTTPS em todas as réplicas;
  - JWT emitido pelo `auth-service` e validado localmente por cada serviço;
  - `X-Service-Key` nas chamadas internas;
  - RBAC por endpoint;
  - registo de auditoria (utilizador, ação, recurso, réplica, resultado).
- **Comunicação:** apenas HTTP/REST síncrono (*commands* e *queries*). Não se usam eventos nem filas de mensagens no Assignment 1.

## Limitações conhecidas

- A classe `Certification` do PSOFT não identifica um modelo de aeronave (requisito US106a). Por isso, a elegibilidade modelo–aeroporto não é validada no agendamento; só o estado operacional dos aeroportos é verificado.
- As réplicas usam H2 em memória: se uma réplica reiniciar, os dados criados nela perdem-se, e só os dados de *bootstrap* são recriados.
- Os dados não são copiados entre réplicas. Se uma réplica estiver em baixo, os registos que só ela tem ficam indisponíveis até voltar; o sistema responde com os dados das réplicas alcançáveis e assinala a resposta como parcial.

## Diagramas

| Ficheiro | Vista |
| --- | --- |
| `aisafe_diagrams/aisafe_ddd_context_map.puml` | Mapa de contextos DDD |
| `aisafe_diagrams/arquitetura.puml` | Arquitetura de containers (C4 nível 2) |
| `aisafe_diagrams/aisafe_deployment_replication.puml` | Vista física: réplicas, portas, HTTPS |
| `aisafe_diagrams/aisafe_peer_get_forwarding_sequence.puml` | Cenário: forwarding de GET e agregação, incluindo falha de peer |
| `aisafe_diagrams/aisafe_create_scheduled_flight_sequence.puml` | Cenário: agendamento de voo entre serviços (US212) |
