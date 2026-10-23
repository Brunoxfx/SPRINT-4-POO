# MOTIVA - Sprint 4 de Programação Orientada a Objetos

API REST para monitorar a vegetação em trechos de rodovia, indicar o tipo de intervenção necessário e manter o
histórico dos relatórios de prioridade no banco de dados Oracle.

O projeto continua o trabalho da Sprint 3 ([SPRING_3_POO](https://github.com/Brunoxfx/SPRING_3_POO)). O protótipo de
console com JDBC puro foi migrado para Spring Boot + Spring Data JPA, reaproveitando as tabelas Oracle modeladas na
Sprint 3.

## Equipe

| Nome | RM |
|---|---|
| Bruno Anselmo da Silva | RM 566521 |
| Fernando de Almeida Godoi | RM 564820 |
| Gabriel Ber Soares | RM 563520 |
| Guilherme de Freitas Salgado | RM 562494 |
| Vinicius Ribeiro Dias | RM 566468 |

## Funcionalidades implementadas

- CRUD REST de equipes de manutenção, trechos de rodovia e intervenções operacionais;
- validações da Sprint 1 (KM, altura da vegetação, ambiente, risco, dados da equipe) na camada `@Service`;
- motor de prioridade da Sprint 2/3 classificando os trechos em urgente, crítico, atenção ou normal;
- geração do relatório de prioridades por endpoint, com gravação no histórico do Oracle;
- consulta do histórico completo e por período;
- filtros por derived queries do Spring Data (altura mínima, risco, ambiente, equipe, tipo de intervenção);
- tratamento global de erros com respostas JSON padronizadas.

## Implementação técnica

- Java 17 e Maven;
- Spring Boot 4.1.2 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`);
- driver Oracle pela dependência Maven `com.oracle.database.jdbc:ojdbc17` (sem JAR manual);
- entidades JPA com `@Entity`, `@Table`, `@Id`, `@GeneratedValue` e `@SequenceGenerator`;
- repositories como interfaces `JpaRepository`, sem nenhuma linha de SQL;
- DTOs (`record`) separando as entidades do contrato da API;
- Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Positive`, `@PositiveOrZero`, `@Size`);
- `@RestControllerAdvice` para tratamento global de erros;
- testes de API com `@SpringBootTest` + `MockMvc` e teste unitário do motor de prioridade.

### Da Sprint 3 para a Sprint 4

| Sprint 3 (JDBC puro) | Sprint 4 (Spring Boot) |
|---|---|
| `model/` + records `*Registro` dos DAOs | `@Entity` em `model/` |
| `EquipeManutencaoDAO`, `TrechoRodoviaDAO`, ... | `EquipeManutencaoRepository`, `TrechoRodoviaRepository`, ... |
| `ConexaoBD` (Singleton) | `application.properties` + pool HikariCP do Spring |
| `service/MotorPrioridade` e `service/GeradorRelatorio` | os mesmos, agora `@Service` |
| `Main.main()` com `System.out.println` | `@RestController` + JSON |
| teste manual no console | cURL / Postman + testes MockMvc |

## Regras de prioridade

Mantidos os limiares da Sprint 2/3. A classificação está em `MotorPrioridade` (`@Service`), nunca no Controller.

| Condição | Prioridade | Intervenção |
|---|---|---|
| Vegetação com 80 cm ou mais e risco alto | URGENTE | Roçada manual (`ROCADA_MANUAL`) |
| Vegetação com 80 cm ou mais e risco baixo | CRITICO | Roçada mecanizada (`ROCADA_MECANIZADA`) |
| Vegetação entre 40 cm e 79,9 cm | ATENCAO | Pulverização (`PULVERIZACAO`) |
| Vegetação abaixo de 40 cm | NORMAL | Sem intervenção |

## Estrutura do projeto

```text
.
|-- src/main/java/br/com/motiva/
|   |-- MotivaApplication.java
|   |-- model/
|   |-- repository/
|   |-- service/
|   |-- controller/
|   |-- dto/
|   `-- exception/
|-- src/main/resources/application.properties
|-- src/test/java/br/com/motiva/
|-- docs/
|-- bd_motiva_criacao.sql
|-- bd_motiva_dados.sql
|-- bd_motiva_sequences.sql
|-- pom.xml
`-- README.md
```

### Pacotes

| Pacote | Responsabilidade |
|---|---|
| `model` | entidades JPA mapeadas para as tabelas da Sprint 3 |
| `repository` | interfaces `JpaRepository` com derived queries |
| `service` | validações de negócio, motor de prioridade e geração do relatório |
| `controller` | endpoints REST e tratamento global de erros |
| `dto` | records de entrada (`*Request`) e saída (`*Response`) da API |
| `exception` | exceções de negócio convertidas em 400, 404 e 409 |

Cada camada só conversa com a vizinha: Controller -> Service -> Repository -> Banco.

### Derived queries

| Repository | Métodos |
|---|---|
| `TrechoRodoviaRepository` | `findByAlturaVegetacaoCmGreaterThanEqual`, `findByRiscoOperacional`, `findByTipoAmbiente`, `findByEquipeManutencaoId`, `existsByKmInicialAndKmFinal` |
| `IntervencaoOperacionalRepository` | `findByTipo`, `findByTrechoId`, `findByEquipeManutencaoId`, `existsByTrechoId` |
| `EquipeManutencaoRepository` | `findByEspecialidadeContainingIgnoreCase`, `findByQuantidadeIntegrantesGreaterThanEqual`, `existsByNomeIgnoreCase` |
| `RelatorioPrioridadeRepository` | `findAllByOrderByDataGeracaoDesc`, `findByDataGeracaoBetweenOrderByDataGeracaoAsc` |

## Banco de dados

As quatro tabelas da Sprint 3 são reaproveitadas sem alteração:

- `TB_EQUIPE_MANUTENCAO`;
- `TB_TRECHO_RODOVIA`;
- `TB_INTERVENCAO_OPERACIONAL`;
- `TB_RELATORIO_PRIORIDADE`.

Na Sprint 3 as chaves eram geradas por `IDENTITY`. Como a Sprint 4 exige `@SequenceGenerator`, o script
`bd_motiva_sequences.sql` cria as sequences `SQ_EQUIPE_MANUTENCAO`, `SQ_TRECHO_RODOVIA`, `SQ_INTERVENCAO_OPERACIONAL`
e `SQ_RELATORIO_PRIORIDADE`, cada uma começando após o maior ID já gravado. As colunas `IDENTITY` foram criadas com
`GENERATED BY DEFAULT ON NULL`, então aceitam os IDs vindos das sequences.

Em um schema novo, execute nesta ordem:

1. `bd_motiva_criacao.sql` - cria as tabelas, chaves e restrições (mesmo script da Sprint 3);
2. `bd_motiva_dados.sql` - inclui os dados iniciais (mesmo script da Sprint 3);
3. `bd_motiva_sequences.sql` - cria as sequences usadas pelo JPA.

No schema que já tem as tabelas da Sprint 3, execute apenas o passo 3. O Hibernate roda com
`spring.jpa.hibernate.ddl-auto=none`: ele não cria nem altera tabelas.

## Configuração da conexão

O `application.properties` lê a conexão das mesmas variáveis de ambiente da Sprint 3, então nenhuma credencial fica no
código-fonte ou no repositório:

```powershell
$env:MOTIVA_DB_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
$env:MOTIVA_DB_USER = "SEU_RM"
$env:MOTIVA_DB_PASSWORD = "SUA_SENHA"
```

## Execução

Com o Java 17 configurado (`java -version`) e as variáveis de ambiente definidas:

```powershell
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Testes

```powershell
mvn test
```

Os testes usam H2 em memória (modo Oracle), configurado em `src/test/resources/application.properties`, e não
precisam do banco da FIAP.

| Classe de teste | Cenários |
|---|---|
| `EquipeManutencaoControllerTest` | 11 |
| `TrechoRodoviaControllerTest` | 16 |
| `IntervencaoOperacionalControllerTest` | 15 |
| `RelatorioPrioridadeControllerTest` | 9 |
| `MotorPrioridadeTest` | 11 |

Resultado em 23/10/2026: 62 testes, 0 falhas.

## Endpoints

### Equipes de manutenção - `/api/equipes`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/equipes` | Lista todas (filtro opcional `?especialidade=`) | 200 | - |
| GET | `/api/equipes/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/equipes` | Cria | 201 + corpo + `Location` | 400, 409 nome duplicado |
| PUT | `/api/equipes/{id}` | Atualiza | 200 | 400, 404, 409 |
| DELETE | `/api/equipes/{id}` | Remove | 204 | 404, 409 se houver vínculos |

### Trechos de rodovia - `/api/trechos`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/trechos` | Lista todos (filtros opcionais `?alturaMinima=`, `?risco=`, `?ambiente=`, `?equipeId=`) | 200 | 400, 404 equipe |
| GET | `/api/trechos/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/trechos` | Cria | 201 + corpo + `Location` | 400, 404 equipe, 409 KM duplicado |
| PUT | `/api/trechos/{id}` | Atualiza | 200 | 400, 404, 409 |
| DELETE | `/api/trechos/{id}` | Remove | 204 | 404, 409 se houver intervenções |

### Intervenções operacionais - `/api/intervencoes`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/intervencoes` | Lista todas (filtros opcionais `?tipo=`, `?trechoId=`, `?equipeId=`) | 200 | 400 tipo inválido |
| GET | `/api/intervencoes/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/intervencoes` | Cria | 201 + corpo + `Location` | 400, 404 trecho/equipe |
| PUT | `/api/intervencoes/{id}` | Atualiza | 200 | 400, 404 |
| DELETE | `/api/intervencoes/{id}` | Remove | 204 | 404 |

Tipos aceitos: `ROCADA_MANUAL`, `ROCADA_MECANIZADA`, `PULVERIZACAO`.

### Relatórios de prioridade - `/api/relatorios`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/relatorios` | Gera o relatório a partir dos trechos atuais e grava no histórico | 201 + corpo + `Location` | - |
| GET | `/api/relatorios` | Lista o histórico (mais recente primeiro) | 200 | - |
| GET | `/api/relatorios/{id}` | Busca um relatório do histórico | 200 | 404 |
| GET | `/api/relatorios/periodo?inicio=AAAA-MM-DD&fim=AAAA-MM-DD` | Consulta o histórico por período | 200 | 400 |

### Formato dos erros

```json
{
  "dataHora": "2026-10-23T10:43:17.8099388",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Trecho de rodovia com ID 999 nao encontrado.",
  "caminho": "/api/trechos/999",
  "detalhes": []
}
```

| Código | Quando |
|---|---|
| 200 | consulta ou atualização realizada |
| 201 | registro criado (corpo + header `Location`) |
| 204 | registro removido |
| 400 | Bean Validation, regra de negócio, JSON mal formatado ou parâmetro inválido |
| 404 | ID inexistente |
| 409 | nome/KM duplicado ou exclusão de registro com vínculos |

## Exemplos cURL

No PowerShell, use `curl.exe` (o `curl` do PowerShell é um alias de `Invoke-WebRequest`).

```bash
# Equipes
curl -i -X POST http://localhost:8080/api/equipes -H "Content-Type: application/json" -d "{\"nome\":\"Equipe Gama\",\"quantidadeIntegrantes\":5,\"especialidade\":\"Controle preventivo\"}"
curl -i http://localhost:8080/api/equipes
curl -i http://localhost:8080/api/equipes/1
curl -i -X PUT http://localhost:8080/api/equipes/1 -H "Content-Type: application/json" -d "{\"nome\":\"Equipe Gama\",\"quantidadeIntegrantes\":7,\"especialidade\":\"Rocada e controle preventivo\"}"
curl -i -X DELETE http://localhost:8080/api/equipes/1

# Trechos
curl -i -X POST http://localhost:8080/api/trechos -H "Content-Type: application/json" -d "{\"kmInicial\":90.0,\"kmFinal\":91.0,\"alturaVegetacaoCm\":85.0,\"tipoAmbiente\":\"umido\",\"riscoOperacional\":\"alto\",\"equipeId\":1}"
curl -i http://localhost:8080/api/trechos
curl -i "http://localhost:8080/api/trechos?alturaMinima=80"
curl -i http://localhost:8080/api/trechos/1
curl -i -X PUT http://localhost:8080/api/trechos/1 -H "Content-Type: application/json" -d "{\"kmInicial\":90.0,\"kmFinal\":91.0,\"alturaVegetacaoCm\":45.0,\"tipoAmbiente\":\"umido\",\"riscoOperacional\":\"baixo\",\"equipeId\":1}"
curl -i -X DELETE http://localhost:8080/api/trechos/1

# Intervencoes
curl -i -X POST http://localhost:8080/api/intervencoes -H "Content-Type: application/json" -d "{\"tipo\":\"PULVERIZACAO\",\"trechoId\":1,\"equipeId\":1}"
curl -i http://localhost:8080/api/intervencoes
curl -i "http://localhost:8080/api/intervencoes?tipo=ROCADA_MANUAL"
curl -i http://localhost:8080/api/intervencoes/1
curl -i -X PUT http://localhost:8080/api/intervencoes/1 -H "Content-Type: application/json" -d "{\"tipo\":\"ROCADA_MECANIZADA\",\"trechoId\":1,\"equipeId\":1}"
curl -i -X DELETE http://localhost:8080/api/intervencoes/1

# Relatorios
curl -i -X POST http://localhost:8080/api/relatorios
curl -i http://localhost:8080/api/relatorios
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-01&fim=2026-10-31"

# Erros esperados
curl -i http://localhost:8080/api/trechos/999
curl -i -X POST http://localhost:8080/api/trechos -H "Content-Type: application/json" -d "{\"kmInicial\":10.0,\"kmFinal\":11.0,\"alturaVegetacaoCm\":-5.0,\"tipoAmbiente\":\"seco\",\"riscoOperacional\":\"baixo\"}"
```

Exemplo de resposta do `POST /api/relatorios`:

```json
{
  "relatorio": {
    "id": 1,
    "quantidadeUrgente": 1,
    "quantidadeCritico": 1,
    "quantidadeAtencao": 0,
    "quantidadeNormal": 0,
    "resumo": "Urgente: 1, Critico: 1, Atencao: 0, Normal: 0",
    "dataGeracao": "2026-10-23T10:43:17.9036586"
  },
  "itens": [
    {
      "trechoId": 1,
      "descricaoKm": "KM 10.0 ao KM 11.0",
      "alturaVegetacaoCm": 92.0,
      "tipoAmbiente": "umido",
      "riscoOperacional": "alto",
      "prioridade": "URGENTE",
      "tipoIntervencao": "ROCADA_MANUAL",
      "intervencaoIndicada": "Rocada manual",
      "motivo": "Vegetacao alta em area com risco operacional alto."
    }
  ]
}
```

## Registro das requisições

Os prints das requisições executadas contra o Oracle FIAP ficam em [`docs/prints/`](docs/prints/).

O arquivo [`docs/validacao-local-h2.md`](docs/validacao-local-h2.md) traz o registro completo de 15 requisições
(CRUD, filtros, relatório, histórico, período e erros 400/404) executadas com cURL durante a validação local.

## Perguntas de reflexão

### 1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?

Porque o Repository só declara o contrato de acesso aos dados: quais operações existem (`save`, `findById`,
`findByTipo`...), sem dizer como elas são feitas. Quem escreve a implementação é o Spring Data JPA, na inicialização
da aplicação: ele cria um proxy em tempo de execução para cada interface que estende `JpaRepository`, usando a
implementação padrão (`SimpleJpaRepository`) para o CRUD e montando as consultas dos métodos derivados a partir do
nome. Esse proxy vira um bean injetado nos Services. Por isso o `TrechoRodoviaRepository` tem poucas linhas, enquanto
o `TrechoRodoviaDAO` da Sprint 3 tinha mais de 100.

### 2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma?

Só mudou de forma. A ideia do DAO continua a mesma: isolar o acesso ao banco em um objeto próprio, para que as regras
de negócio não conheçam SQL nem conexão. Na Sprint 3 nós escrevíamos esse objeto à mão (SQL, `PreparedStatement`,
`ResultSet`, conversão para record). Na Sprint 4 o Repository é o DAO, mas a implementação é gerada pelo framework.
O `GeradorRelatorio`, por exemplo, continua chamando um objeto de persistência para salvar o relatório; só trocou
`relatorioDAO.salvarRelatorio(...)` por `relatorioRepository.save(relatorio)`.

### 3. Por que a validação de altura da vegetação >= 0 (Sprint 1) deve ficar no Service e não no Controller?

Porque é uma regra de negócio do domínio, e não um detalhe do protocolo HTTP. O Controller é só uma das portas de
entrada do sistema; o Service pode ser chamado por outro Controller, por um job agendado ou por um teste, e a regra
precisa valer em todos esses casos. Se ela ficasse no Controller, bastaria outro ponto de entrada para gravar uma
altura negativa no banco. No nosso projeto o `TrechoRodoviaRequest` só exige que a altura seja informada
(`@NotNull`), e a regra "a altura da vegetacao nao pode ser negativa" é validada em `TrechoRodoviaService`, que
responde 400 pelo `@RestControllerAdvice`. Assim o Controller fica só com a tradução HTTP <-> DTO, como pede a
arquitetura em camadas.

### 4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?

Não está escrito em lugar nenhum do código. O Spring Data JPA lê o nome do método na inicialização (`find` + `By` +
propriedade `tipo`), monta a consulta JPQL equivalente a `select i from IntervencaoOperacional i where i.tipo = ?1` e
o Hibernate a traduz para o SQL do Oracle, algo como
`SELECT ... FROM TB_INTERVENCAO_OPERACIONAL WHERE TIPO = ?`, usando o mapeamento `@Table`/`@Column` da entidade.
Com `spring.jpa.show-sql=true` esse SQL gerado aparece no console a cada chamada.

## Boas práticas de Git

- commits incrementais no padrão Conventional Commits (`chore:`, `feat:`, `test:`, `docs:`);
- `.gitignore` ignorando `target/`, arquivos de IDE (`.idea/`, `*.iml`, `.vscode/`) e `.env`;
- nenhuma senha no repositório: a conexão usa variáveis de ambiente.
