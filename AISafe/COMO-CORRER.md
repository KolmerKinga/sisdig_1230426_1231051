# AISafe distribuído — como correr e testar

## Serviços e portas

| Serviço | Réplica 1 | Réplica 2 | Protocolo | Depende de |
|---|---|---|---|---|
| auth-service | 8051 | 8052 | HTTPS | — |
| airport-service | 8091 | 8092 | HTTPS | (token do auth) |
| maintenance-service | 8061 | 8062 | HTTPS | aircraft-service, (token do auth) |
| Aircraft_Service | 8081 | 8082 | HTTP | — |
| route_flight_service | (ver o README dele) | | | |

Todos os serviços HTTPS usam o mesmo certificado (`AISafe/certs`), a mesma chave JWT e a mesma chave de serviço.

## 1. Preparar (uma vez por computador)

```bash
cd AISafe
./scripts/gen-certs.sh          # Windows: scripts\gen-certs.bat
```
Cria `AISafe/certs/`, que nunca vai para o git.

Mac: `export JAVA_HOME=$(/usr/libexec/java_home -v 17)`. Windows: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"`.

## 2. Arrancar

**Opção A: tudo de uma vez** (compila e arranca 2 réplicas de cada um dos 3 serviços):
```bash
./scripts/run-all.sh            # Mac/Linux: corre em segundo plano, logs em AISafe/logs/
./scripts/stop-all.sh           # para tudo
```
```powershell
scripts\run-all.bat             # Windows: abre uma janela por réplica
```

**Opção B: à mão**, um terminal por réplica, dentro da pasta do serviço:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=instance1
mvn spring-boot:run -Dspring-boot.run.profiles=instance2
```
(no PowerShell: `mvn spring-boot:run "-Dspring-boot.run.profiles=instance1"`)

**Ordem recomendada:**
1. `auth-service`
2. `Aircraft_Service`
3. `airport-service`
4. `maintenance-service`

Os serviços arrancam por qualquer ordem, mas os testes precisam do login e da frota.

O `Aircraft_Service` arranca à parte, seguindo o README dele (por agora só a instância 1, na porta 8081). Sem ele, criar registos de manutenção e os alertas dão `503`.

**Parar e voltar a arrancar só uma réplica** (para os testes de falha, Mac):
```bash
kill $(lsof -t -iTCP:8062 -sTCP:LISTEN)       # pára só a maintenance-2 (trocar a porta para outra réplica)
cd maintenance-service && java -jar target/maintenance-service.jar --spring.profiles.active=instance2   # volta a arrancá-la
```
Usar sempre `-sTCP:LISTEN`: sem isso, o `lsof` também devolve a réplica peer (tem ligações abertas para essa porta) e o `kill` pára as duas.
O `stop-all.sh` pára tudo o que estiver nas portas 8051/8052, 8061/8062 e 8091/8092, mesmo que não tenha sido arrancado pelo `run-all.sh`. O Aircraft_Service pára-se com `Ctrl+C` no terminal dele.

## 3. Verificar que estão vivos

```bash
curl -k https://localhost:8051/actuator/health     # auth-1
curl -k https://localhost:8061/actuator/health     # maintenance-1
curl -k https://localhost:8091/actuator/health     # airport-1
```

## 4. Teste rápido com curl (Mac)

```bash
# login -> token
T=$(curl -sk -X POST https://localhost:8051/auth/login -H "Content-Type: application/json" \
     -d '{"username":"technician","password":"technician123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# templates das duas réplicas, pedidos à réplica 1
curl -sk -H "Authorization: Bearer $T" https://localhost:8061/api/maintenance/templates

# o mesmo token funciona noutro serviço, que nunca falou com o auth-service
curl -sk -i -H "Authorization: Bearer $T" "https://localhost:8061/api/maintenance/records?aircraft=CS-TMB"
```

## 5. Postman

1. **Settings → General → desligar "SSL certificate verification"** (o certificado é self-signed).
2. Importar as três coleções de `AISafe/tests/postman/`:
   - `auth-service.postman_collection.json`
   - `airport-service.postman_collection.json`
   - `maintenance-service.postman_collection.json`
3. Em cada coleção, **⋯ → Run collection**. A primeira pasta faz login e **guarda os tokens sozinha**. Já não é preciso colar tokens nem usar scripts.
4. As pastas de **falha** correm-se à parte, depois de parar uma réplica:

| Coleção | Pastas a correr com tudo ligado | Pasta de falha |
|---|---|---|
| auth | 0–3 | 4 (bloqueia o `lockout-demo` 5 minutos; correr no fim) |
| airport | 00–4 | 5: parar o airport-2 |
| maintenance | 00–5 | 6: parar o maintenance-2; 7: parar o Aircraft_Service |

## 6. Utilizadores

| Username | Password | Papel |
|---|---|---|
| admin | admin123 | ADMIN |
| atcc | atcc123 | ATCC |
| backoffice | backoffice123 | BACKOFFICE |
| technician | technician123 | MAINTENANCE_TECHNICIAN |
| supervisor | supervisor123 | MAINTENANCE_SUPERVISOR |

## 7. Guião para a demonstração

1. **Login em réplicas diferentes:** login no auth-1, e `GET /auth/me` no auth-2 com esse token. O token é validado pela chave partilhada, sem sessão partilhada.
2. **Forwarding:** `GET /api/maintenance/templates/{C-Check}` na maintenance-1 → `X-Data-Source: https://localhost:8062`.
3. **Agregação:** `GET /api/maintenance/parts/low-stock?threshold=10` → o alerta da `FLT-OIL-01` só existe porque soma as duas réplicas (6 + 5).
4. **Chamada entre serviços:** criar um registo para o CS-TMA. A manutenção valida a aeronave no Aircraft_Service e copia o modelo. Ao iniciá-lo, a aeronave passa a `MAINTENANCE` (campo `aircraftSync`).
5. **Comandos reencaminhados:** iniciar o registo através da réplica que **não** o tem → `X-Data-Source` mostra a réplica dona.
6. **Falha de réplica:** parar a maintenance-2. Os pedidos continuam a ser respondidos, com `X-Partial-Result: true`.
7. **Falha de dependência:** parar o Aircraft_Service. Criar um registo dá `503`, mas consultar e concluir registos continua a funcionar.
8. **Independência do auth:** parar as duas réplicas do auth-service. Os tokens já emitidos continuam a funcionar em todos os serviços.
9. **Segurança:**
   - pedido sem token → `401`;
   - papel errado → `403`;
   - `X-Forwarded-By` falso → `403`;
   - 5 passwords erradas → `429`.
10. **Auditoria:** mostrar `maintenance-service/logs/audit-maintenance-1.log`.

## 8. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `SSL Error: self signed certificate` no Postman | Desligar *SSL certificate verification* |
| O serviço não arranca: `...aisafe-keystore.p12 (No such file)` | Falta correr `gen-certs`, ou a pasta de trabalho não é a do serviço (no IntelliJ: *Working directory* = pasta do serviço) |
| `Port 8061 was already in use` | Ficou uma réplica antiga a correr: `kill $(lsof -t -iTCP:8061 -sTCP:LISTEN)` (Mac). Usar sempre `-sTCP:LISTEN`, senão também mata a réplica peer, que tem ligações abertas para essa porta |
| `401` em todos os pedidos | O token expirou (1 h): correr outra vez a pasta de login |
| Criar um registo de manutenção dá `503` | O Aircraft_Service não está a correr em 8081/8082 |
| `keytool` não encontrado | Ver a secção 1 (JAVA_HOME / PATH do JDK 17) |
