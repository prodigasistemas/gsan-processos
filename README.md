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

**Projetos relacionados**

* gsan-persistence(O projeto tem uma dependencia do maven para a biblioteca **gsan-persistence** será necessario instalar o projeto gsan-persistence.)
  


**Tecnologias utilizadas**

* Java 8
* Servidor de aplicação: WildFly 8.0.0
* Banco de dados: Postgres 9.3.4


**Instalações**

* Configuração da aplicação Java: Instale o driver do postgres no Wildfly (pode usar a lib armazenada na pasta migracoes/drivers do projeto gsan-persistence)
* Crie um datasource no arquivo standalone-full.xml com o jndi 'java:jboss/datasources/GsanDS' (o mesmo do persistence.xml)
* Execute a aplicação com a versão full do wildfly (standalone-full.xml)

**Instalação do Apache Archiva**

[Apache Archiva](https://github.com/prodigasistemas/gsan/wiki/Instala%C3%A7%C3%A3o-do-Archiva)

**Configuraçao do Apache Archiva (Opcional):**

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




