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
| `EquipeManutencaoControllerTest` | 12 |
| `TrechoRodoviaControllerTest` | 20 |
| `IntervencaoOperacionalControllerTest` | 15 |
| `RelatorioPrioridadeControllerTest` | 9 |
| `MotorPrioridadeTest` | 11 |

Resultado em 23/10/2026: 67 testes, 0 falhas.

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

Os filtros dos `GET` de trechos e intervenções são usados **um por vez**. Se mais de um for informado, vale apenas
o primeiro na ordem em que aparecem na tabela (por exemplo, `?alturaMinima=80&risco=alto` filtra só pela altura).

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

Há duas formas de demonstrar a API. As duas criam os próprios registros, usam os IDs devolvidos pela API (nada de
supor ID 1 num schema reaproveitado) e removem no fim apenas o que criaram, na ordem intervenção -> trechos ->
equipe.

- **Postman (recomendado para os prints):** importe a coleção
  [`docs/postman/motiva-sprint4.postman_collection.json`](docs/postman/motiva-sprint4.postman_collection.json) e siga
  o guia [`docs/postman/README.md`](docs/postman/README.md).
- **cURL:** o roteiro abaixo roda no **Git Bash** (Windows), Linux ou macOS. Copie e cole o bloco inteiro, na ordem.
  Ele usa a equipe `Equipe Gama` e os KMs 90-91 e 92-93; se já existirem no seu schema, troque esses valores.

```bash
API=http://localhost:8080/api
JSON='Content-Type: application/json'

# Mostra a resposta completa (status, headers e corpo) e guarda o id criado em $ID
post() {
  RESPOSTA=$(curl -s -i -X POST "$API$1" -H "$JSON" ${2:+-d "$2"})
  echo "$RESPOSTA"; echo
  ID=$(echo "$RESPOSTA" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
}

# 1. Equipes
post /equipes '{"nome":"Equipe Gama","quantidadeIntegrantes":5,"especialidade":"Controle preventivo"}'
EQUIPE_ID=$ID
curl -si "$API/equipes"
curl -si "$API/equipes/$EQUIPE_ID"
curl -si "$API/equipes?especialidade=preventivo"
curl -si -X PUT "$API/equipes/$EQUIPE_ID" -H "$JSON" \
  -d '{"nome":"Equipe Gama","quantidadeIntegrantes":7,"especialidade":"Rocada e controle preventivo"}'

# 2. Trechos (um URGENTE e um ATENCAO)
post /trechos '{"kmInicial":90.0,"kmFinal":91.0,"alturaVegetacaoCm":85.0,"tipoAmbiente":"umido","riscoOperacional":"alto","equipeId":'"$EQUIPE_ID"'}'
TRECHO_URGENTE_ID=$ID
post /trechos '{"kmInicial":92.0,"kmFinal":93.0,"alturaVegetacaoCm":55.0,"tipoAmbiente":"seco","riscoOperacional":"baixo"}'
TRECHO_ATENCAO_ID=$ID
curl -si "$API/trechos"
curl -si "$API/trechos/$TRECHO_URGENTE_ID"
curl -si "$API/trechos?alturaMinima=80"
curl -si -X PUT "$API/trechos/$TRECHO_ATENCAO_ID" -H "$JSON" \
  -d '{"kmInicial":92.0,"kmFinal":93.0,"alturaVegetacaoCm":60.0,"tipoAmbiente":"seco","riscoOperacional":"baixo","equipeId":'"$EQUIPE_ID"'}'

# 3. Intervencoes
post /intervencoes '{"tipo":"ROCADA_MANUAL","trechoId":'"$TRECHO_URGENTE_ID"',"equipeId":'"$EQUIPE_ID"'}'
INTERVENCAO_ID=$ID
curl -si "$API/intervencoes"
curl -si "$API/intervencoes/$INTERVENCAO_ID"
curl -si "$API/intervencoes?tipo=ROCADA_MANUAL"
curl -si -X PUT "$API/intervencoes/$INTERVENCAO_ID" -H "$JSON" \
  -d '{"tipo":"ROCADA_MECANIZADA","trechoId":'"$TRECHO_URGENTE_ID"',"equipeId":'"$EQUIPE_ID"'}'

# 4. Relatorios
post /relatorios
RELATORIO_ID=$ID
curl -si "$API/relatorios"
curl -si "$API/relatorios/$RELATORIO_ID"
HOJE=$(date +%F)
curl -si "$API/relatorios/periodo?inicio=$HOJE&fim=$HOJE"

# 5. Erros esperados
curl -si "$API/trechos/999999"                                  # 404
curl -si -X POST "$API/trechos" -H "$JSON" \
  -d '{"kmInicial":94.0,"kmFinal":95.0,"alturaVegetacaoCm":-5.0,"tipoAmbiente":"seco","riscoOperacional":"baixo"}'  # 400
curl -si -X DELETE "$API/trechos/$TRECHO_URGENTE_ID"            # 409: o trecho tem intervencao

# 6. Limpeza (intervencao -> trechos -> equipe)
curl -si -X DELETE "$API/intervencoes/$INTERVENCAO_ID"          # 204
curl -si -X DELETE "$API/trechos/$TRECHO_URGENTE_ID"            # 204
curl -si -X DELETE "$API/trechos/$TRECHO_ATENCAO_ID"            # 204
curl -si -X DELETE "$API/equipes/$EQUIPE_ID"                    # 204
curl -si "$API/equipes/$EQUIPE_ID"                              # 404
```

No PowerShell, o `curl` é um alias de `Invoke-WebRequest` e o escape de aspas é diferente. Nesse caso, use o Postman
ou o `Invoke-RestMethod`, que já devolve o JSON como objeto:

```powershell
$api = "http://localhost:8080/api"
$equipe = Invoke-RestMethod -Method Post -Uri "$api/equipes" -ContentType "application/json" `
    -Body (@{ nome = "Equipe Gama"; quantidadeIntegrantes = 5; especialidade = "Controle preventivo" } | ConvertTo-Json)
$trecho = Invoke-RestMethod -Method Post -Uri "$api/trechos" -ContentType "application/json" `
    -Body (@{ kmInicial = 90.0; kmFinal = 91.0; alturaVegetacaoCm = 85.0; tipoAmbiente = "umido";
              riscoOperacional = "alto"; equipeId = $equipe.id } | ConvertTo-Json)
Invoke-RestMethod -Uri "$api/trechos/$($trecho.id)"
Invoke-RestMethod -Method Post -Uri "$api/relatorios" | ConvertTo-Json -Depth 5
Invoke-RestMethod -Method Delete -Uri "$api/trechos/$($trecho.id)"
Invoke-RestMethod -Method Delete -Uri "$api/equipes/$($equipe.id)"
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

### Prints do Postman (Oracle FIAP)

Coleção [`docs/postman/motiva-sprint4.postman_collection.json`](docs/postman/motiva-sprint4.postman_collection.json)
executada no aplicativo Postman em 23/10/2026, com a API em `http://localhost:8080` conectada ao Oracle FIAP
(schema da Sprint 3). As 43 requisições foram enviadas em ordem, todas com os testes da coleção aprovados.

| Nº | Requisição | Método e rota | Status | Print |
|---|---|---|---|---|
| 01 | Criar equipe | `POST /api/equipes` | 201 | [01-postman.png](docs/prints/01-postman.png) |
| 02 | Criar trecho urgente | `POST /api/trechos` | 201 | [02-postman.png](docs/prints/02-postman.png) |
| 09 | Listar trechos | `GET /api/trechos` | 200 | [09-postman.png](docs/prints/09-postman.png) ([b](docs/prints/09-postman-b.png), [c](docs/prints/09-postman-c.png), [d](docs/prints/09-postman-d.png)) |
| 15 | Atualizar equipe | `PUT /api/equipes/{id}` | 200 | [15-postman.png](docs/prints/15-postman.png) |
| 17 | Criar intervenção | `POST /api/intervencoes` | 201 | [17-postman.png](docs/prints/17-postman.png) |
| 24 | Gerar relatório | `POST /api/relatorios` | 201 | [24-postman.png](docs/prints/24-postman.png) ([b](docs/prints/24-postman-b.png), [c](docs/prints/24-postman-c.png)) |
| 27 | Consultar período | `GET /api/relatorios/periodo` | 200 | [27-postman.png](docs/prints/27-postman.png) |
| 29 | Erro: altura negativa | `POST /api/trechos` | 400 | [29-postman.png](docs/prints/29-postman.png) |
| 34 | Excluir intervenção | `DELETE /api/intervencoes/{id}` | 204 | [34-postman.png](docs/prints/34-postman.png) |
| 35 | Erro: intervenção excluída | `GET /api/intervencoes/{id}` | 404 | [35-postman.png](docs/prints/35-postman.png) |

O relatório gerado (ID 5) classificou os 8 trechos do banco em 2 urgentes, 2 críticos, 2 em atenção e 2 normais.
Ao final, as exclusões da coleção devolveram o banco ao estado original da Sprint 3 (2 equipes, 4 trechos e
3 intervenções); os relatórios permanecem no histórico.

Validação em 23/10/2026: **43 requisições e 62 verificações passaram no Oracle FIAP**, com dez prints e
registro HTTP completo. O relatório também foi consultado após reiniciar a API, confirmando a persistência.
Veja os [resultados e as evidências da validação Oracle](docs/validacao-oracle.md).

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
