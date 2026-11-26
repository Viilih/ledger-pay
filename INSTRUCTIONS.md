# Ledger Pay

Aplicação Java 21 com Spring Boot, Maven e PostgreSQL 17.

O livro-razão e o fluxo de uma transferência estão em [docs/ledger.md](docs/ledger.md).

## Desenvolvimento local

Pré-requisitos: JDK 21, Docker com Docker Compose v2 e Bash para os comandos abaixo.
Execute os comandos na raiz do projeto.

1. Crie sua configuração local, caso ainda não tenha um `.env`:

   ```bash
   cp -n .env.example .env
   ```

2. Edite o `.env` e preencha `DB_PASSWORD` com uma senha exclusiva de desenvolvimento.
   Para senhas com caracteres especiais, use aspas simples, por exemplo
   `DB_PASSWORD='sua-senha-local'`. Mantenha os demais campos assim:

   | Variável | Valor local |
      | --- | --- |
   | `DB_URL` | `jdbc:postgresql://localhost:5432/ledger_pay` |
   | `DB_NAME` | `ledger_pay` |
   | `DB_USERNAME` | `ledgerpay` |
   | `DB_PASSWORD` | Sua senha local |

   O `.env` é ignorado pelo Git. Versione somente o `.env.example`, sem senhas reais.
   Se alterar `DB_NAME`, atualize também o nome do banco no final de `DB_URL`.

3. Inicie o banco e aguarde o health check:

   ```bash
   docker compose up -d --wait
   docker compose ps
   ```

   O Compose lê o `.env` automaticamente e exige nome, usuário e senha preenchidos.
   A porta 5432 fica disponível apenas na máquina local; ela precisa estar livre.
   Os dados ficam no volume `postgres_data`.

4. Exporte as variáveis para o processo Java e inicie a aplicação:

   ```bash
   set -a
   source .env
   set +a
   ./mvnw spring-boot:run
   ```

   O Spring Boot recebe `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` pelo ambiente;
   ele não carrega o `.env` automaticamente. Os comandos acima carregam o seu
   arquivo local na sessão Bash atual. No IntelliJ, configure essas mesmas três
   variáveis no campo **Environment variables** da configuração de execução.

5. Para executar os testes, com o banco iniciado e as variáveis exportadas:

   ```bash
   ./mvnw -B verify
   ```

   Os testes locais usam o banco indicado em `DB_URL`. Para testar com outro banco,
   crie uma instância separada e ajuste as variáveis antes de executar o Maven.

Para parar o banco preservando os dados:

```bash
docker compose down
```

As variáveis `POSTGRES_*` inicializam o banco somente quando o volume está vazio.
Alterar a senha no `.env` depois da primeira execução não altera a senha já gravada
no PostgreSQL. Para mudar as credenciais sem perder dados, altere também o usuário
no banco. Se quiser começar do zero, `docker compose down -v` apaga os dados locais;
depois execute novamente `docker compose up -d --wait`.

## Configurar o GitHub Actions

O workflow `.github/workflows/maven.yml` cria um PostgreSQL temporário para cada job,
aguarda o health check e executa o build com testes. O CI obtém seus valores das
configurações do GitHub, independentemente do `.env` local.

### 1. Cadastrar as Variables

No repositório do GitHub:

1. Acesse **Settings → Secrets and variables → Actions**.
2. Abra a aba **Variables**.
3. Clique em **New repository variable** para cada item abaixo:

   | Name | Value sugerido |
      | --- | --- |
   | `CI_DB_NAME` | `ledger_pay_test` |
   | `CI_DB_USERNAME` | `ledgerpay` |

4. Confirme cada cadastro em **Add variable**.

### 2. Cadastrar a senha como Secret

1. Na mesma página, abra a aba **Secrets**.
2. Clique em **New repository secret**.
3. Em **Name**, informe `CI_DB_PASSWORD`.
4. Em **Secret**, preencha uma senha exclusiva para o banco de CI, sem aspas extras.
5. Clique em **Add secret**.

O workflow usa `${{ vars.CI_DB_NAME }}` e `${{ vars.CI_DB_USERNAME }}` para os valores
comuns e `${{ secrets.CI_DB_PASSWORD }}` para a senha. A mesma senha é fornecida ao
container PostgreSQL e ao processo Java. Não é necessário cadastrar `DB_URL`: o CI
monta a URL com `localhost:5432` e `CI_DB_NAME`.

Esses valores precisam existir antes da execução: os serviços são iniciados antes
dos steps. Uma senha ausente impede a inicialização do container PostgreSQL.

### 3. Executar e conferir

1. Envie as alterações para a branch e abra um pull request destinado à `main`,
   ou faça um push na `main`, conforme os gatilhos do workflow.
2. Abra **Actions → Java CI with Maven** e selecione a execução.
3. Confira a inicialização do serviço PostgreSQL e o step **Build with Maven**.
4. O resultado esperado é `BUILD SUCCESS`, com o teste de inicialização do Spring passando.

Secrets não são fornecidos por padrão a pull requests de forks nem a execuções do
Dependabot. O workflow atual exige o Secret e não cobre esses casos. Para aceitar
contribuições externas, será necessário definir uma estratégia de banco de teste
que não dependa de Secrets do repositório.

## Produção

Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` na plataforma de deploy, usando um
banco e credenciais próprios de produção. A senha deve vir do gerenciador de
segredos da plataforma. O Compose deste repositório atende ao desenvolvimento local;
o serviço PostgreSQL do GitHub Actions atende somente aos testes de CI.

## Referências

- [Variáveis e interpolação no Docker Compose](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/)
- [Configuração externa no Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Variables no GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-variables)
- [Secrets no GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets)
