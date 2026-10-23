# Demonstracao da API no Postman

Importe `motiva-sprint4.postman_collection.json` pelo botao **Import** do Postman.
A colecao usa o formato v2.1 e possui 43 requisicoes, com verificacao automatica dos status HTTP.

## Preparacao

1. Prepare as sequences e configure a conexao Oracle conforme o README da raiz.
2. Inicie a API com Java 17: `mvn spring-boot:run`.
3. Na colecao importada, confira `baseUrl`: `http://localhost:8080`.
4. Use um schema de demonstracao sem a equipe `Equipe Gama` nem os pares de KM 90–91, 92–93, 94–95 e 96–97.
   Esses valores seguem os exemplos da atividade. Se ja existirem, ajuste os corpos de criacao e atualizacao antes
   de executar; nao apague dados anteriores apenas para liberar os exemplos.
5. Execute pelo **Collection Runner**, em ordem e com uma iteracao. Pare na primeira falha para verificar a causa.
   Nao configure variaveis de ambiente Postman com os mesmos nomes das variaveis da colecao.

Os IDs de equipe, quatro trechos, intervencao e relatorio sao preenchidos pelos scripts das respostas 201.
As datas do periodo vem da data do relatorio, evitando datas fixas e divergencia de fuso entre cliente e servidor.
Execute desde o inicio: as consultas dependem dos IDs capturados. Os filtros sao testados individualmente.

As ultimas chamadas removem somente os cadastros criados na demonstracao, na ordem intervencao -> trechos -> equipe.
O relatorio permanece no historico, pois a atividade nao define endpoint para exclui-lo.
Uma interrupcao antes dessas chamadas deixa os cadastros de demonstracao no banco; use os IDs retornados para
concluir as exclusoes, sem remover outros registros.

## Cobertura

| Grupo | Requisicoes |
|---|---|
| Preparacao | equipe e quatro trechos, um por prioridade |
| Equipes | criar, listar, buscar, filtrar especialidade, atualizar, remover |
| Trechos | criar, listar, buscar, filtros altura/risco/ambiente/equipe, atualizar, remover |
| Intervencoes | criar, listar, buscar, filtros tipo/trecho/equipe, atualizar, remover |
| Relatorios | gerar, historico, buscar por ID, periodo, consultar novamente apos exclusoes |
| Erros | 400 nas tres entidades e periodo; 404 apos exclusoes; 409 ao excluir equipe/trecho em uso |

Os scripts verificam status, Location nas criacoes, corpo vazio nas exclusoes e as quatro prioridades.
Historico e periodo precisam conter o ID do relatorio gerado. Os testes nao exigem contagens globais fixas:
o schema pode conter outros trechos da Sprint 3.

## Registros para a entrega

Salve em `docs/prints/` pelo menos estas oito respostas executadas contra Oracle FIAP:

1. **01 - Criar equipe**: 201, corpo e Location.
2. **02 - Criar trecho urgente**: 201, corpo e Location.
3. **09 - Listar trechos**: 200.
4. **15 - Atualizar equipe**: 200, quantidade alterada para 7.
5. **17 - Criar intervencao**: 201.
6. **24 - Gerar relatorio**: 201, resumo e itens.
7. **27 - Consultar periodo**: 200, relatorio gerado presente.
8. **34 - Excluir intervencao**: 204.

Inclua tambem as chamadas 29 (400) e 35 (404), se desejar evidenciar os erros previstos.
Cada print deve mostrar metodo, URL, status e corpo (quando houver). Nao inclua senhas, variaveis de conexao
ou paineis com credenciais. A colecao nao armazena credenciais Oracle: a conexao pertence ao servidor Spring.

## Execucao por linha de comando

Com Newman instalado, tambem e possivel executar a mesma colecao e exportar o registro:

```powershell
newman run docs/postman/motiva-sprint4.postman_collection.json --bail --reporters cli,json --reporter-json-export resultado-postman.json
```

Para uma porta diferente, acrescente `--env-var baseUrl=http://localhost:18084`.
O registro JSON contem requisicoes, respostas e resultados das assercoes.

## Validacao

Em 23/10/2026, Newman 6 executou esta colecao contra a API H2 na porta 18084:
**43 requisicoes, 62 assercoes, zero falhas**. A colecao foi lida e executada pelo Newman sem erro de formato.
Os 19 pares metodo/rota da tabela de endpoints foram exercitados, incluindo as variantes de filtro.
O processo Spring foi iniciado com Java 17 e `mvn -o spring-boot:run`, usando o classpath de testes
e `src/test/resources/application.properties`. Nenhuma configuracao de producao foi alterada.
Importacao visual no aplicativo Postman e execucao no Oracle pertencem a T4; nao sao comprovadas por testes H2.
