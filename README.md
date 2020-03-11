gsan-processos
==============

Módulo de gerenciamento dos processos do Gsan

**Verificador de Processos**
  
  * processos não agendados 
  * existe processo agendado mas não disponivel para processar
  * existe processo agendado pronto para processar

**Gerenciador e Verificador de log**

  * visualiza mensagens de determinado processo para o usuario 
  * armazena mensagens no banco
  * visualiza todas mensagens no log do servidor 
  
O `gsan-processos` realiza o gerenciamento dos agendamentos e o controle das filas e das configurações de execução dos processos para serem adicionados a fila de execução dos processos em batch. Para isso o `gsan-processos` possui um agendador que verifica periodicamente os processos configurados e adicionados no banco de dados para alterar seu status e adicionar na fila de execução.

Ainda no escopo do projeto está o gerenciamento do log dos processos que são capturados a partir das mensagens enviadas pelos processos batch e compilados no arquivo de log do processo.

As filas que são referenciadas para envio dos processos pendentes de processamento e das mensagens de log, utilizam a especificação do JMS (Java Message Service) e essas filas são usadas como meio de comunicação entre os projetos `gsan-processos` e os `batch` (exemplo: `gsan-batch`).

A seguir estão algumas informações para a configuração e execução do projeto.


**Estrutura dos Processos no Banco de Dados**

  Especificação de novos processos no sistema:
  
  * Processo (tabela: batch.processo) - Registro que define um processo, onde o mesmo será composto por uma ou mais atividades (processo_atividade), exemplo de registro novo:
  
> INSERT INTO batch.processo(
	proc_id, proc_dsprocesso, proc_dsabreviado, proc_icuso, proc_tmultimaalteracao, prtp_id, proc_dsorientacao, proc_icautorizacao, proc_limite, proc_nmarquivobatch, proc_prioridade)
	VALUES (nextval('batch.seq_processo'), 'ESTATISTICAS IMOVEIS', 'ESIM', 1, now(), 4, null, 2, 0, ‘job_estatisticas_imoveis’, 0);
  
  * Processo Atividade (tabela: batch.processo_atividade) - Registro que define uma atividade de um processo e os parâmetros de configuração da atividade como concorrência, ordem de execução e limite de execução simultânea, exemplo de registro novo:
  
> INSERT INTO batch.processo_atividade(
	id, nmarquivobatch, limiteexecucao, descricao, ordemexecucao, exibiremtela, proc_id, tmultimaalteracao, principal_id, processa_varios_itens)
	VALUES (nextval('batch.processo_atividade'), 'job_estatisticas_imoveis', 10, ‘ESTATISTICAS IMOVEIS’, 1, 1, 511, now(), null, true);
	
  Para adicionar um processo para execução e reconhecimento do processo PENDENTE pelo `gsan-processos` segue a definição das tabelas usadas:
  
  * Processo Iniciado (tabela: batch.processo_iniciado) - Registro que identifica o processo que foi adicionado para iniciar execução, status inicial AGENDADO, exemplo de registro novo:
  
> INSERT INTO batch.processo_iniciado(
	proi_id, proi_idprecedente, proc_id, proi_tmagendamento, proi_tminicio, proi_tmtermino, proi_tmcomando, prst_id, usur_id, proi_tmultimaalteracao, proi_nngrupo, proi_prioridade, proi_execution_id)
	VALUES (nextval('batch.seq_processo_iniciado'), null, 511, null, null, null, null, 4, 529, now(), null, 0, 0);
	
  Obs: O valor `511` corresponde ao processo cadastrado na etapa anterior; `4` é id do processo situação AGENDADO; e `529` é o id de um usuário de exemplo. Esses valores precisam ser consultados para preencher a query de exemplo adequadamente.
  
  * Controle Processo Atividade (tabela: batch.controle_processo_atividade) - Registro que identifica o processo atividade (atividades que compõem um processo) que foi adicionado para iniciar execução, status inicial AGENDADO, exemplo de registro novo:
  
> INSERT INTO batch.controle_processo_atividade(
	id, inicio, termino, situacao, totalitens, itensprocessados, proa_id, proi_id, tmultimaalteracao)
	VALUES (nextval('batch.seq_controle_processo_atividade'), '2020-02-26 23:51:00', null, 4, null, null, 39, 149999, now());
	
  Obs: O valor `4` corresponde a situação AGENDADO da atividade; `39` é id do processo atividade cadastrado na etapa de definição do processo; e `149999` é o id do `processo_iniciado` cadastrado na etapa anterior. Esses valores precisam ser consultados para preencher a query de exemplo adequadamente.
  
  * Processo Parametros (tabela: batch.processo_parametros) - Registro usado para repassar parâmetros de execução para o processo batch, os parâmetros default idProcessoIniciado e nomeArquivoBatch são adicionados automaticamente pelo Verificador Processo quando marca o ProcessoIniciado e ControleProcessoAtividade para EM_ESPERA e adiciona na fila de execução.

> INSERT INTO batch.processo_parametros(
	prpr_id, proi_id, prpr_nmparametro, prpr_valorparametro, prpr_temporario)
	VALUES (nextval(‘batch.seq_processo_parametros’), 149999, ‘idRota’, ‘1’, null);
	
  Obs: O valor `149999` é o id do `processo_iniciado` cadastrado na etapa anterior; o `idRota` é a descrição do parâmetro que poderá ser usado durante a execução do processo; e `1` é o valor do parâmetro. Esses valores precisam ser consultados para preencher a query de exemplo adequadamente.

**Projetos relacionados**

  * gsan-persistence - O projeto tem uma dependencia do maven para a biblioteca **gsan-persistence** será necessário instalar o projeto.


**Tecnologias utilizadas**

  * Java 8
  * Servidor de aplicação: WildFly 8.0.0
  * Banco de dados: Postgres 9.3.4


**Configurações e Implantação**

  Instale o driver do postgres no Wildfly (pode usar a lib armazenada na pasta migracoes/drivers do projeto gsan-persistence):
  
  * Crie a pasta `modules/org/postgresql/main` e adicione o arquivo `module.xml` com o conteúdo abaixo (o exemplo abaixo está com a versão 8.4 do postgresql):

	<?xml version="1.0" encoding="UTF-8"?>
	
	<module xmlns="urn:jboss:domain:datasources:2.0" name="org.postgresql">
	     <resources>
	     	<resource-root path="postgresql-8.4-703.jdbc4.jar"/>
	     </resources>
	    <dependencies>
	        <module name="javax.api"/>
	        <module name="javax.transaction.api"/>
	    </dependencies>
	</module> 
	
  * Copie o arquivo .jar da lib do postgresql para a pasta do wildfly `modules/org/postgresql/main`.

  * Crie um datasource no arquivo `standalone/configuration/standalone-full.xml` com o jndi 'java:jboss/datasources/GsanDS' (o mesmo do persistence.xml)

	<subsystem xmlns="urn:jboss:domain:datasources:2.0">
        <datasources>
            <datasource jta="false" jndi-name="java:/jboss/datasources/GsanDS" pool-name="GsanDS" enabled="true" use-ccm="false">
                <connection-url>jdbc:postgresql://[HOST DO BANCO]:[PORTA]/[BASE DE DADOS]</connection-url>
                <driver>pg</driver>
                <security>
                    <user-name>[USERNAME]</user-name>
                    <password>[PASSWORD]</password>
                </security>
            </datasource>
            <drivers>
                <driver name="pg" module="org.postgresql">
                    <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
                </driver>
            </drivers>
        </datasources>
    </subsystem>

  Gerar o build do `gsan-processos` através do maven (no exemplo abaixo os testes estão sendo ignorados, pois precisam ser estruturados novamente):
  
> mvn clean install -Dmaven.test.skip=true
  	
  Copiar o build gerado na pasta `target/gsan-processos-0.0.1.war` para a pasta `standalone/deployments`
  
  Execute a aplicação com a versão full do wildfly (standalone-full.xml) a partir da raiz do projeto o comando abaixo:
	
> ./bin/standalone.sh -c standalone-full.xml


**Outras Informações:**

Para os ambientes que não são de Desenvolvimento será necessário um repositório para as libs externas como o `gsan-persistence`.

*Instalação do Apache Archiva*

[Apache Archiva](https://github.com/prodigasistemas/gsan/wiki/Instala%C3%A7%C3%A3o-do-Archiva)

*Configuraçao do Apache Archiva:*

Crie ou altere o arquivo settings.xml na pasta .m2 do Maven:

    <servers>   
      <server>
      <id>archiva.internal</id>
      <username>:usuario</username>
      <password>:senha</password>
     </server>
    </servers>  
    <profiles>   
      <profile>
        <id>Repository Proxy</id>
        <activation>
          <activeByDefault>true</activeByDefault>
        </activation>
        <!-- place repo xml here -->
    <repositories>
     <repository>
       <id>internal</id>
       <name>Archiva Managed Internal Repository</name>
       <url>http://archiva.[nome_repositorio]/repository/internal/</url>
      <releases>
       <enabled>true</enabled>
      </releases>
      <snapshots>
       <enabled>true</enabled>
      </snapshots>
     </repository>
    </repositories>
    <pluginRepositories>
       <pluginRepository>
         <id>internal</id>
           <name>Archiva Managed Internal Repository</name>
           <url>http://archiva.[nome_repositorio]/repository/internal/</url>
         <releases>
           <enabled>true</enabled>
         </releases>
          <snapshots>
           <enabled>true</enabled>
         </snapshots>
       </pluginRepository>
      </pluginRepositories>
     </profile>
    </profiles>   
    
** Possíveis Problemas**

A demora no deploy pode gerar algumas exceções de Timeout na execução do Timer, isso precisa ser analisado.




