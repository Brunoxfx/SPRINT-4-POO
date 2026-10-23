# Validacao no Oracle FIAP

Execucao em 23/10/2026, com Java 17, Spring Boot 4.1.2, driver Maven `ojdbc17` e Oracle 19c.
A API foi iniciada com `mvn -o spring-boot:run`, porta local 18084, usando as variaveis `MOTIVA_DB_*`.
O perfil H2 nao foi utilizado nesta demonstracao. Credenciais nao fazem parte dos artefatos.

## Resultado

- `mvn -o test`: **67 testes, zero falhas e zero erros** (suite automatizada H2).
- Colecao Postman executada pelo Newman contra a API conectada ao Oracle: **43 requisicoes e 62 assercoes,
  zero falhas**. Inclui os 19 pares metodo/rota, filtros, quatro prioridades e respostas 200/201/204/400/404/409.
- Cinco verificacoes extras no Oracle retornaram 400: altura NaN, filtro NaN, KMs com tres casas decimais,
  altura acima de `NUMBER(10,2)` e equipe com 1000 integrantes.
- Relatorio de ID **4** consultado novamente depois de encerrar e reiniciar o processo Java.
  ID e resumo conferem com a resposta original. Uma consulta independente ao Oracle confirmou a mesma linha.

## Preservacao do banco

As tabelas existentes da Sprint 3 foram reaproveitadas; `ddl-auto=none` foi mantido.
As quatro sequences ainda nao existiam. Foi executado o bloco de `bd_motiva_sequences.sql`,
com inicio em `MAX(ID) + 1` para cada tabela, sem recriar tabelas ou dados.

| Tabela | Antes | Depois |
|---|---:|---:|
| TB_EQUIPE_MANUTENCAO | 2 | 2 |
| TB_TRECHO_RODOVIA | 4 | 4 |
| TB_INTERVENCAO_OPERACIONAL | 3 | 3 |
| TB_RELATORIO_PRIORIDADE | 2 | 3 |

Os cadastros temporarios da demonstracao foram excluidos pela API na ordem correta.
O novo relatorio permanece no historico, junto dos dois anteriores. A API nao oferece exclusao de relatorios.

## Evidencias

Os PNGs abaixo sao capturas de paginas que exibem os registros HTTP reais exportados pelo Newman.
Nao sao capturas da interface do Postman. Os arquivos HTML correspondentes e o JSON bruto permitem conferir
metodo, URL, corpo enviado, status, Location e resposta recebida.

| Chamada | Status | Print |
|---|---:|---|
| Criar equipe | 201 | [01-oracle.png](prints/01-oracle.png) |
| Criar trecho urgente | 201 | [02-oracle.png](prints/02-oracle.png) |
| Listar trechos | 200 | [09-oracle.png](prints/09-oracle.png) |
| Atualizar equipe | 200 | [15-oracle.png](prints/15-oracle.png) |
| Criar intervencao | 201 | [17-oracle.png](prints/17-oracle.png) |
| Gerar relatorio | 201 | [24-oracle.png](prints/24-oracle.png) |
| Consultar periodo | 200 | [27-oracle.png](prints/27-oracle.png) |
| Rejeitar altura negativa | 400 | [29-oracle.png](prints/29-oracle.png) |
| Excluir intervencao | 204 | [34-oracle.png](prints/34-oracle.png) |
| Buscar intervencao excluida | 404 | [35-oracle.png](prints/35-oracle.png) |

- [Registro completo das 43 chamadas](prints/registro-oracle.json).
- [Persistencia apos reinicio e verificacoes de limites](prints/persistencia-e-limites.json).
- [Colecao e instrucoes de execucao](postman/README.md).

A porta 18084 foi usada somente nesta validacao; a configuracao padrao continua em 8080.

## Execucao manual no Postman

Em 23/10/2026, as 43 requisicoes da colecao tambem foram enviadas uma a uma no aplicativo Postman, com a API na
porta padrao 8080 conectada ao Oracle FIAP. Todos os testes da colecao passaram. O relatorio gerado nessa execucao
(ID 5) classificou 2 trechos em cada prioridade. Depois das exclusoes, a API listou 2 equipes, 4 trechos,
3 intervencoes e 4 relatorios, ou seja, os dados da Sprint 3 mais o historico de relatorios.

As capturas de tela do Postman estao listadas na secao "Prints do Postman (Oracle FIAP)" do README
e salvas como `docs/prints/*-postman*.png`.
