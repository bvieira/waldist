================ Arquitetura =================
São duas servlets, uma responsável pelo gerenciamento das informações e outra responsável pela busca de rotas.
Todo processamento é feito pelo DistributionServices, que é responsável pelas regras de negócio e acessa o DAO para a aquisição dos dados. Para a adição das malhas logísticas, foi implementada uma fila que garante que não haverá duas atualizações simultaneas no mesmo mapa.  
O algoritmo utilizado para calcular a menor rota entre os pontos foi o Dijkstra, implementado no próprio Neo4j.  

=> Tecnologias
	Logback: logs, melhor performance que o log4j e placeholders :) 
	PicoContainer: responsável pela injeção de dependências, optei por utiliza-lo pois é uma countainer leve e também já tinha boas experiências profissionais
	Neo4j: base de dados, a estrutura de dados em grafo do sistema encaixa perfeitamente no Neo4j, já havia tido ótimas experiências com ele, a api é extremamente fácil de usar.
	Gson: reponsável pela manipulação dos jsons, é uma biblioteca leve e fácil de usar
	JUnit + Mockito: testes unitários, api fácil de usar
	
=> Logs
/tmp/waldist/logs/waldist.log

=> Database
/tmp/waldist/database/	

================ Documentação ================
Optei em fazer o javadoc apenas para alguns metodos, tentei fazer a implementação mais simples e fluida possível, para que o próprio codigo se auto-explique. 

=================== Testes ===================
Além dos testes efetuados pelo JUnity, também criei uma massa de testes disponível em doc/example/
Executei também uns testes de carga chamando diversos curls de maneira assíncrona

================== Execução ==================
=> Ambiente
	Java 1.7.0_51
	Apache Maven 3.1.1
	
=> Compilar
	~/distribution-services > mvn clean install
	
=> Iniciando o server
	~/distribution-services > mvn jetty:run-forked
	
=> Parando o server
	~/distribution-services > mvn jetty:stop

=================== Ações ====================

======= Cadastro das Malhas logísticas =======

=> GET /distribution-map/
	lista as malhas logísticas disponíveis, retorna a resposta em json na estrutura abaixo:
	["%nome mapa 1%", "%nome mapa 2%", ...]
	
	ex: 
		> curl -X GET http://localhost:8080/distribution-map/
		< ["map1", "map2", "map3"]
	
	=> DELETE /distribution-map/
	remove todas as malhas logísticas cadastradas
	
	ex:
		> curl -X DELETE http://localhost:8080/distribution-map/
		<
		
=> GET /distribution-map/[nome da malha]
	lista as caminhos cadastrados para malha logística, retorna a resposta em json na estrutura abaixo:
	{
		"map": "{%nome do mapa%}",
		"paths":[
			{"start": "{%nome do centro de distribuicao 1%}", "end": "{%nome do centro de distribuicao 2%}", distance: {%distancia 1%}},
			{"start": "{%nome do centro de distribuicao 2%}", "end": "{%nome do centro de distribuicao 3%}", distance: {%distancia 2%}}
		]
	}
	
	ex:
		> curl -X GET http://localhost:8080/distribution-map/map1
		< {"map":"map1","paths":[{"start":"A","end":"B","distance":10.0},{"start":"A","end":"C","distance":20.0},{"start":"B","end":"D","distance":15.0},{"start":"C","end":"D","distance":30.0}]}
	
=> POST /distribution-map/[nome da malha]
	cadastra a malha logística, com o nome que foi passado. Caso a malha já exista, o conteúdo da malha cadastrada anteriormente será removido e o conteúdo enviado será adicionado.
	obs: No corpo da requisição deve ser enviado as rotas da malha logística seguindo o seguinte padrão: ponto de origem, ponto de destino e distância entre os pontos, conforme exemplificado abaixo:
	A B 10
	B D 15.5
	A C 20
	C D 30
	obs2: devido a limitações do formato de entrada, o nome dos pontos não podem ser palavras compostas
	obs3: a distância é medida em kilometros
	
	
	ex:
		> curl -X POST --data-binary @map.txt http://localhost:8080/distribution-map/map1
		<

=> DELETE /distribution-map/[nome da malha]
	remove a malha logística que tiver o nome determinado na requisição
	
	ex:
		> curl -X DELETE http://localhost:8080/distribution-map/map1
		<

============ Busca de menor rota =============

=> GET /routes?map={%nome da malha%}&start={%origem%}&end={%destino%}&performance={%autonomia em km/l%}&price={%preço da gasolina%}
	A resposta da menor rota será enviada no formato json seguindo a estrutura abaixo:
	{
		"route": ["{%nome do centro de distribuicao 1%}", "{%nome do centro de distribuicao 2%}", "{%nome do centro de distribuicao 1%}"],
		"distance": {%distancia%},
		"price": {%preço%}
	}
	
	ex:
		> curl -X GET "http://localhost:8080/routes?map=map1&start=A&end=D&performance=10&price=2.5"
		< {"route":["A","B","D"],"distance":25.0,"price":6.25}