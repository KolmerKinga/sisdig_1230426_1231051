# airport-service — Airport Management

Serviço do contexto **Airport Management** do AISafe distribuído (SIDIS, Assignment 1). É dono dos aeroportos, pistas, instalações, contactos, estado operacional e certificações dos aeroportos (PSOFT US106, US106a, US107, US108, US109, US207, US208, US211, US225).

É um projeto Spring Boot **independente**: tem o seu próprio `pom.xml`, base de dados e configuração, e não importa classes de nenhum outro serviço. **Não chama outros serviços**; é chamado pelo `route-flight-service` (`GET /api/airports/{iataCode}`).

## Como correr

Pré-requisitos: JDK 17 e Maven.

1. **Gerar o certificado HTTPS** (uma vez, partilhado por todos os serviços). É criado em `AISafe/certs/`, que não vai para o git:
   ```bash
   cd AISafe
   ./scripts/gen-certs.sh        # Windows: scripts\gen-certs.bat
   ```
2. **Arrancar as duas réplicas**, cada uma num terminal, dentro de `AISafe/airport-service`:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=instance1   # https://localhost:8091
   mvn spring-boot:run -Dspring-boot.run.profiles=instance2   # https://localhost:8092
   ```
   Em alternativa: `mvn package` e depois `java -jar target/airport-service.jar --spring.profiles.active=instance1`.
3. **Obter um token.** Enquanto o `auth-service` não existir, use o token de desenvolvimento:
   ```bash
   python AISafe/scripts/dev-token.py --user ana --roles BACKOFFICE
   python AISafe/scripts/dev-token.py --user rui --roles ATCC
   ```
4. **Testar** com a coleção `AISafe/tests/postman/airport-service.postman_collection.json`. No Postman, desligar *SSL certificate verification*, porque o certificado é self-signed.

Verificação rápida: `curl -k https://localhost:8091/actuator/health`.

## Réplicas e dados

| Réplica | Perfil | Porta | Peers | Base de dados | Dados iniciais |
|---|---|---|---|---|---|
| airport-1 | `instance1` | 8091 | 8092 | `jdbc:h2:mem:airport_8091` | OPO, LIS, MAD |
| airport-2 | `instance2` | 8092 | 8091 | `jdbc:h2:mem:airport_8092` | CDG, LHR, FAO |

Os dados estão **particionados**: cada aeroporto vive na réplica que o criou. É por isso que cada réplica arranca com aeroportos diferentes, para se ver o forwarding.

## Endpoints

| Método e caminho | US | Papéis | Distribuição |
|---|---|---|---|
| `POST /api/airports` | 106, 207 | BACKOFFICE, ADMIN | Guarda nesta réplica; antes verifica nos peers se o IATA já existe (409) |
| `POST /api/airports/import` (multipart `file`) | 225 | BACKOFFICE, ADMIN | Igual ao anterior, linha a linha; devolve um relatório. Exemplo em `samples/airports-import.csv` |
| `GET /api/airports/{iataCode}` | 107 | BACKOFFICE, ATCC, ADMIN | Local; se não existir, encaminha aos peers |
| `GET /api/airports?city=&country=&name=&page=&size=` | 108 | ATCC, ADMIN | Agrega local + peers, sem duplicados, ordena e pagina |
| `GET /api/airports/grouped?by=country\|city\|region` | 211 | ATCC, ADMIN | Agrega local + peers e agrupa |
| `PATCH /api/airports/{iataCode}/status` | 109 | BACKOFFICE, ADMIN | Aplica onde o aeroporto está (local ou peer) |
| `PATCH /api/airports/{iataCode}/details` | 208 | BACKOFFICE, ADMIN | Aplica onde o aeroporto está (local ou peer) |
| `POST /api/airports/{iataCode}/certifications` | 106a | BACKOFFICE, ATCC, ADMIN | Aplica onde o aeroporto está (local ou peer) |
| `GET /actuator/health` | — | público | — |
| `GET /swagger-ui.html` | — | público | OpenAPI |

Diferenças face ao PSOFT:
- Os endpoints `GET /api/airports/{iata}/routes` e `GET /api/airports/busiest` passaram para o `route-flight-service`, que é dono das rotas.
- O campo `routeDistance` foi removido: as distâncias pertencem às rotas.
- O `operationalHours` passou a ser guardado (no PSOFT era ignorado).

## Forwarding entre réplicas

Implementado em `shared/forwarding/PeerClient`.

- **Um recurso:** procura primeiro no H2 local. Se não estiver, envia o mesmo pedido aos peers e devolve a resposta de quem o tem.
- **Coleções:** consulta todos os peers em paralelo e junta os resultados por código IATA (a cópia local ganha).
- **Sem ciclos:** o pedido reencaminhado leva `X-Forwarded-By`. Quem o recebe responde só com dados locais e nunca o volta a reencaminhar (no máximo 1 salto).
- **Tolerância a falhas:**
  - timeout de ligação de 1 s e de leitura de 2 s;
  - 2 tentativas com backoff de 200 ms;
  - circuit breaker: um peer que falha é ignorado durante 10 s.
- **Disponibilidade primeiro (AP):**
  - um 404 só é dado quando todas as réplicas alcançáveis dizem "não existe";
  - se algum peer falhou, a resposta leva `X-Partial-Result: true`;
  - a criação de um aeroporto continua mesmo que um peer não possa ser verificado, e a resposta vem marcada como parcial.

Cabeçalhos de resposta úteis na demonstração:

| Cabeçalho | Significado |
|---|---|
| `X-Served-By` | Réplica que atendeu o pedido |
| `X-Data-Source` | `local` ou URL do peer que tinha o dado |
| `X-Partial-Result` | `true` se algum peer não respondeu |
| `X-Total-Count` | Total de resultados antes da paginação |
| `X-Correlation-Id` | Identificador do pedido, igual nos logs de todas as réplicas |

## Segurança

- **HTTPS** (TLS 1.3) em todas as portas, com o bundle SSL `aisafe`. O mesmo certificado é usado pelo cliente HTTP que fala com os peers.
- **JWT:**
  - emitido pelo `auth-service` e validado localmente com a chave partilhada (`AISAFE_JWT_SECRET`);
  - não há chamadas ao `auth-service` em cada pedido;
  - claims: `sub`, `username`, `roles`.
- **Autenticação entre serviços:** um pedido interno (com `X-Forwarded-By` ou `X-Service-Key`) tem de trazer a chave correta (`AISAFE_SERVICE_KEY`), caso contrário recebe 403. O JWT do utilizador é sempre propagado, por isso o RBAC é aplicado em todas as réplicas.
- **RBAC** por endpoint (tabela acima). Qualquer caminho não listado é recusado.
- **Auditoria:** uma linha por chamada à API em `logs/audit-<instância>.log`, com utilizador, papéis, ação, estado, réplica, quem reencaminhou e duração.
- Os erros nunca expõem stack traces.

## Variáveis de ambiente

| Variável | Valor por omissão (só para desenvolvimento) |
|---|---|
| `AISAFE_JWT_SECRET` | segredo de desenvolvimento (Base64) |
| `AISAFE_SERVICE_KEY` | `dev-service-key-change-me` |
| `AISAFE_CERTS_DIR` | `../certs` |
| `AISAFE_KEYSTORE_PASSWORD` | `changeit` |
| `AISAFE_INSTANCEID`, `SERVER_PORT`, `AISAFE_PEERS` | definidos pelo perfil `instanceN`; servem para o docker-compose (ex.: `AISAFE_PEERS=https://airport-2:8092`) |

## Estrutura

```
airport-service/
├── pom.xml
├── samples/airports-import.csv
└── src/
    ├── main/java/pt/isep/aisafe/airport/
    │   ├── AirportServiceApplication.java
    │   ├── api/             controller REST, DTOs, parser CSV
    │   ├── application/     casos de uso (sobre os dados locais), bootstrap, exceções
    │   ├── domain/          Airport (agregado), Runway, Certification, value objects
    │   ├── infrastructure/  AirportRepository (JPA)
    │   └── shared/          config, forwarding, security, audit, error
    ├── main/resources/      application*.properties, logback, seed/*.json
    └── test/java/           testes de domínio e do parser CSV
```

## Limitações conhecidas

- O H2 é em memória: se uma réplica reiniciar, perde os aeroportos criados nela (os dados de seed voltam a ser carregados).
- Os dados não são copiados entre réplicas. Se uma réplica estiver em baixo, os aeroportos que só ela tem ficam indisponíveis até voltar.
- A `Certification` não identifica um modelo de aeronave (PSOFT US106a), por isso a elegibilidade modelo–aeroporto não é validada.
