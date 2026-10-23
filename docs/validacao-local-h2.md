# Validacao local da API (H2 em memoria)

Execucao de 23/10/2026 com `mvn spring-boot:run` apontando para H2 em memoria (modo Oracle), apenas para conferir os
codigos HTTP antes da execucao no Oracle FIAP. Os prints oficiais da entrega ficam em `docs/prints/`.

```text

$ curl -X POST http://localhost:8080/api/equipes -d '{"nome":"Equipe Alfa","quantidadeIntegrantes":6,"especialidade":"Rocada manual"}'
HTTP/1.1 201 
Location: http://localhost:8080/api/equipes/1
Content-Type: application/json

{"id":1,"nome":"Equipe Alfa","quantidadeIntegrantes":6,"especialidade":"Rocada manual"}


$ curl -X POST http://localhost:8080/api/trechos -d '{"kmInicial":10.0,"kmFinal":11.0,"alturaVegetacaoCm":92.0,"tipoAmbiente":"umido","riscoOperacional":"alto","equipeId":1}'
HTTP/1.1 201 
Location: http://localhost:8080/api/trechos/1
Content-Type: application/json

{"id":1,"kmInicial":10.0,"kmFinal":11.0,"descricaoKm":"KM 10.0 ao KM 11.0","alturaVegetacaoCm":92.0,"tipoAmbiente":"umido","riscoOperacional":"alto","equipeId":1,"equipeNome":"Equipe Alfa"}


$ curl -X POST http://localhost:8080/api/trechos -d '{"kmInicial":20.0,"kmFinal":21.0,"alturaVegetacaoCm":55.0,"tipoAmbiente":"seco","riscoOperacional":"baixo"}'
HTTP/1.1 201 
Location: http://localhost:8080/api/trechos/2
Content-Type: application/json

{"id":2,"kmInicial":20.0,"kmFinal":21.0,"descricaoKm":"KM 20.0 ao KM 21.0","alturaVegetacaoCm":55.0,"tipoAmbiente":"seco","riscoOperacional":"baixo","equipeId":null,"equipeNome":null}


$ curl -X GET http://localhost:8080/api/trechos 
HTTP/1.1 200 
Content-Type: application/json

[{"id":1,"kmInicial":10.0,"kmFinal":11.0,"descricaoKm":"KM 10.0 ao KM 11.0","alturaVegetacaoCm":92.0,"tipoAmbiente":"umido","riscoOperacional":"alto","equipeId":1,"equipeNome":"Equipe Alfa"},{"id":2,"kmInicial":20.0,"kmFinal":21.0,"descricaoKm":"KM 20.0 ao KM 21.0","alturaVegetacaoCm":55.0,"tipoAmbiente":"seco","riscoOperacional":"baixo","equipeId":null,"equipeNome":null}]


$ curl -X GET http://localhost:8080/api/trechos?alturaMinima=80 
HTTP/1.1 200 
Content-Type: application/json

[{"id":1,"kmInicial":10.0,"kmFinal":11.0,"descricaoKm":"KM 10.0 ao KM 11.0","alturaVegetacaoCm":92.0,"tipoAmbiente":"umido","riscoOperacional":"alto","equipeId":1,"equipeNome":"Equipe Alfa"}]


$ curl -X PUT http://localhost:8080/api/trechos/2 -d '{"kmInicial":20.0,"kmFinal":21.0,"alturaVegetacaoCm":85.0,"tipoAmbiente":"seco","riscoOperacional":"baixo","equipeId":1}'
HTTP/1.1 200 
Content-Type: application/json

{"id":2,"kmInicial":20.0,"kmFinal":21.0,"descricaoKm":"KM 20.0 ao KM 21.0","alturaVegetacaoCm":85.0,"tipoAmbiente":"seco","riscoOperacional":"baixo","equipeId":1,"equipeNome":"Equipe Alfa"}


$ curl -X POST http://localhost:8080/api/intervencoes -d '{"tipo":"ROCADA_MANUAL","trechoId":1,"equipeId":1}'
HTTP/1.1 201 
Location: http://localhost:8080/api/intervencoes/1
Content-Type: application/json

{"id":1,"tipo":"ROCADA_MANUAL","nomeIntervencao":"Rocada manual","trechoId":1,"descricaoTrecho":"KM 10.0 ao KM 11.0","equipeId":1,"equipeNome":"Equipe Alfa"}


$ curl -X GET http://localhost:8080/api/intervencoes?tipo=ROCADA_MANUAL 
HTTP/1.1 200 
Content-Type: application/json

[{"id":1,"tipo":"ROCADA_MANUAL","nomeIntervencao":"Rocada manual","trechoId":1,"descricaoTrecho":"KM 10.0 ao KM 11.0","equipeId":1,"equipeNome":"Equipe Alfa"}]


$ curl -X POST http://localhost:8080/api/trechos -d '{"kmInicial":30.0,"kmFinal":29.0,"alturaVegetacaoCm":-5.0,"tipoAmbiente":"seco","riscoOperacional":"baixo"}'
HTTP/1.1 400 
Content-Type: application/json

{"dataHora":"2026-10-23T10:43:17.7176039","status":400,"erro":"Bad Request","mensagem":"O KM final deve ser maior que o KM inicial.","caminho":"/api/trechos","detalhes":[]}


$ curl -X GET http://localhost:8080/api/trechos/999 
HTTP/1.1 404 
Content-Type: application/json

{"dataHora":"2026-10-23T10:43:17.8099388","status":404,"erro":"Not Found","mensagem":"Trecho de rodovia com ID 999 nao encontrado.","caminho":"/api/trechos/999","detalhes":[]}


$ curl -X POST http://localhost:8080/api/relatorios 
HTTP/1.1 201 
Location: http://localhost:8080/api/relatorios/1
Content-Type: application/json

{"relatorio":{"id":1,"quantidadeUrgente":1,"quantidadeCritico":1,"quantidadeAtencao":0,"quantidadeNormal":0,"resumo":"Urgente: 1, Critico: 1, Atencao: 0, Normal: 0","dataGeracao":"2026-10-23T10:43:17.9036586"},"itens":[{"trechoId":1,"descricaoKm":"KM 10.0 ao KM 11.0","alturaVegetacaoCm":92.0,"tipoAmbiente":"umido","riscoOperacional":"alto","prioridade":"URGENTE","tipoIntervencao":"ROCADA_MANUAL","intervencaoIndicada":"Rocada manual","motivo":"Vegetacao alta em area com risco operacional alto."},{"trechoId":2,"descricaoKm":"KM 20.0 ao KM 21.0","alturaVegetacaoCm":85.0,"tipoAmbiente":"seco","riscoOperacional":"baixo","prioridade":"CRITICO","tipoIntervencao":"ROCADA_MECANIZADA","intervencaoIndicada":"Rocada mecanizada","motivo":"Vegetacao alta em area com risco operacional baixo."}]}


$ curl -X GET http://localhost:8080/api/relatorios 
HTTP/1.1 200 
Content-Type: application/json

[{"id":1,"quantidadeUrgente":1,"quantidadeCritico":1,"quantidadeAtencao":0,"quantidadeNormal":0,"resumo":"Urgente: 1, Critico: 1, Atencao: 0, Normal: 0","dataGeracao":"2026-10-23T10:43:17.903659"}]


$ curl -X GET http://localhost:8080/api/relatorios/periodo?inicio=2026-10-23&fim=2026-10-23 
HTTP/1.1 200 
Content-Type: application/json

[{"id":1,"quantidadeUrgente":1,"quantidadeCritico":1,"quantidadeAtencao":0,"quantidadeNormal":0,"resumo":"Urgente: 1, Critico: 1, Atencao: 0, Normal: 0","dataGeracao":"2026-10-23T10:43:17.903659"}]


$ curl -X DELETE http://localhost:8080/api/intervencoes/1 
HTTP/1.1 204 



$ curl -X DELETE http://localhost:8080/api/intervencoes/1 
HTTP/1.1 404 
Content-Type: application/json

{"dataHora":"2026-10-23T10:43:18.3671733","status":404,"erro":"Not Found","mensagem":"Intervencao operacional com ID 1 nao encontrada.","caminho":"/api/intervencoes/1","detalhes":[]}

```
