# Justificação da arquitetura segundo DDD

O AISafe foi organizado em quatro **Bounded Contexts**, seguindo princípios de **Domain-Driven Design (DDD)**. Um Bounded Context delimita uma área de negócio com responsabilidades, regras e dados próprios.

Esta divisão representa a arquitetura distribuída proposta para SIDIS. O projeto PSOFT atual ainda é uma aplicação monolítica.

## Critério de divisão

Os contextos foram definidos pelas responsabilidades de negócio e pela proximidade entre os conceitos do domínio. Conceitos que participam nas mesmas operações e regras ficaram no mesmo contexto.

Por exemplo, os modelos e as aeronaves pertencem à gestão da frota, enquanto os registos e os modelos de manutenção pertencem à gestão das intervenções. As rotas e os voos ficaram juntos porque o agendamento utiliza uma rota e contribui para o seu histórico.

O Assignment apresenta uma divisão possível em três serviços, adaptável à dimensão do grupo. A proposta utiliza quatro serviços, um por contexto. A existência de quatro elementos no grupo facilita a distribuição do trabalho, mas o principal critério é a separação das responsabilidades de negócio.

## Bounded Contexts

| Contexto | Responsabilidade | Principais conceitos do PSOFT |
| --- | --- | --- |
| **Aircraft Management** | Gerir modelos, aeronaves, características e estado operacional da frota. | `AircraftModel`, `Aircraft`, `AircraftAvailability`, `AircraftCertification` |
| **Airport Management** | Gerir aeroportos, pistas, instalações, estado operacional e certificações dos aeroportos. | `Airport`, `Runway`, `Facilities`, `AirportStatus`, `Certification` |
| **Route / Flight Operations** | Gerir rotas, histórico e agendamento de voos, coordenando as verificações necessárias. | `Route`, `RouteHistory`, `Flight` |
| **Maintenance Management** | Gerir modelos de manutenção, registos de intervenções e o respetivo progresso. | `MaintenanceTemplate`, `MaintenanceRecord` |

As listas identificam os principais conceitos; não representam todas as classes de cada contexto. No código PSOFT, o voo agendado é representado pela classe `Flight`.

## Dependências entre contextos

As dependências representam pedidos de informação através de **HTTP/REST**:

- **Route / Flight Operations → Aircraft Management:** verificar a existência da aeronave e consultar estado operacional, autonomia (*range*) e capacidade.
- **Route / Flight Operations → Airport Management:** validar os aeroportos de origem e destino e consultar o seu estado operacional e certificações.
- **Route / Flight Operations → Maintenance Management:** consultar o estado de manutenção e a disponibilidade relacionada com intervenções.
- **Maintenance Management → Aircraft Management:** verificar a existência da aeronave e obter os dados necessários à manutenção, como modelo e horas de voo.
- **Aircraft Management → Route / Flight Operations:** obter informação para consultas de rotas compatíveis, voos e estatísticas.
- **Airport Management → Route / Flight Operations:** obter rotas associadas a aeroportos e estatísticas.

As duas últimas dependências permitem preservar consultas existentes no PSOFT. A arquitetura também prevê que Route / Flight Operations solicite a atualização das métricas de voo a Aircraft Management, que continua a ser responsável pelos dados da aeronave.

## Propriedade dos dados e comunicação

Cada contexto é responsável pela sua lógica de negócio e pelos seus dados. **Não existe uma base de dados partilhada entre serviços**, nem acesso direto aos repositories de outro contexto.

Referências entre contextos devem utilizar identificadores e APIs HTTP/REST. Por exemplo, um voo identifica a aeronave atribuída, mas os dados dessa aeronave continuam a pertencer a Aircraft Management.

Route / Flight Operations coordena as verificações do agendamento. Aircraft Management fornece o estado operacional da aeronave; Maintenance Management fornece informação sobre intervenções. Consultar essa informação não transfere a propriedade dos dados para o contexto que a utiliza.

Esta organização permite desenvolver, testar, executar e escalar cada serviço separadamente, mantendo explícitas as dependências necessárias ao negócio.

## Diferenças face ao PSOFT atual

A proposta define o comportamento pretendido; a separação ainda não está implementada. Na implementação distribuída será necessário:

- Substituir os acessos diretos entre domínios e as relações JPA que atravessam contextos por identificadores e comunicação HTTP/REST.
- Completar os contratos de consulta: o GET atual da aeronave não fornece todos os dados necessários para validar autonomia e capacidade.
- Incluir a verificação explícita da capacidade e a consulta à manutenção no agendamento. Atualmente, a manutenção é considerada através do estado da aeronave, sem consulta direta aos registos.
- Alinhar a certificação modelo–aeroporto com o requisito US106a. A classe `Certification` atual não identifica o modelo de aeronave; é distinta de `AircraftCertification`.

Os endpoints e contratos ainda não definidos permanecem **TBD**. A replicação e o GET forwarding complementam esta divisão, mas são decisões de distribuição, não critérios para definir os Bounded Contexts.

