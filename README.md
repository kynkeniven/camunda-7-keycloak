# garantia-bpms-camunda

### Links uteis
    - http://localhost:8080/ 
    - http://localhost:8080/actuator/health/liveness
    - http://localhost:8080/actuator/health/readiness

### Docker compose para subir o Postgres e o Keycloak
```
docker-compose -f .docker/docker-compose.yml up -d
```

## Etapas de build e execução do projeto

Como realizar o build e rodar o projeto usando maven.

<table>
    <tr>
        <td><b>Target</b></td>
        <td>&nbsp;</td>
        <td><b>Maven Goal</b></td>
    </tr>
    <tr>
        <td>Build Spring Boot Jar</td>    
        <td>&nbsp;</td>
        <td><code>mvn clean install</code></td>    
    </tr>
    <tr>
        <td>Run Spring Boot App</td>
        <td>&nbsp;</td>
        <td><code>mvn spring-boot:run -Dspring-boot.run.profiles=dev
</code></td>
    </tr>

</table>

### Rodar o camunda localmente sem dependência de Banco de dados ou Keycloak
```
SPRING_PROFILES_ACTIVE=local
```

### Rodar o camunda localmente com Banco de dados Postgres e Keycloak
```
SPRING_PROFILES_ACTIVE=dev
```

### Acesso ao Keycloak

URL:
```
http://localhost:9000/auth/
```
1. Login no Keycloak
Usuario: `admin` Senha: `admin`

2. Principais informações sobre a configuração do KC

- Dentro da pasta .docker/keycloak/h2 está o BD com todas as configurações já salvas desse ambiente local, portanto não será preciso fazer nenhuma configuração
- Realm `Camunda` é o realm utilizado pela aplicação (Não o realm Master)
- No painel do KC Clients > camunda-identity-provider, é o client criado para essa aplicação
- Dentro do client, as mudanças realizadas foram nas abas Settings, Client scopes, Service  accounts roles e Advanced
- Menu Realm roles, foram criadas as roles `camunda_admin` e `camunda_user`
- Menu Users, foram  criados dois usuarios: `camunda` e `johndoe`, as respectivas senhas deles são `camunda` e `123456`
- Menu Groups, foram criados os grupos `camunda-admin` e `camunda-user`
- Dentro de cada grupo, na aba Members foram adicionaos os usuarios `camunda` e `johndoe` respectivamente
- Dentro de cada grupo, na aba Role mapping, forma adicionados os realm roles `camunda_admin` e `camunda_user` respectivamente

### Postman Camunda

Na pasta .postman, esta a collection com dois endpoints disponiveis.
- Start Process, com um exemplo de como iniciar um processo no Camunda
- Keycloak Token Generator, exemplo de como gerar o token que dará permissão de executar o endpoint acima de start de processo
- Ao gerar o token, o token ja é automaticamente setado no header do Start Process
