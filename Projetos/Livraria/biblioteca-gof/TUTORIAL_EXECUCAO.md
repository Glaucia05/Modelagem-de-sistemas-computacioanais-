# Tutorial de execução — Biblioteca GoF

Responsável pela finalização: Sérgio Calazans. Entrega: 23/11/2026.

## 1. Pré-requisitos

- JDK Java 17 ou superior, incluindo o compilador `javac`.
- Apache Maven 3.8 ou superior.
- Acesso à internet na primeira execução para baixar dependências.
- MySQL 8 somente para o modo `prod` e para a homologação opcional `mysql-it`.
- Astah UML somente para editar os modelos; não é necessário para compilar ou executar.

Confirme no terminal:

```text
java -version
javac -version
mvn -version
```

O Java mostrado pelo Maven deve ser 17 ou superior. Configure `JAVA_HOME` para a pasta do JDK e inclua `JAVA_HOME/bin` e a pasta `bin` do Maven no PATH. Reinicie o terminal após alterar variáveis do sistema. A validação desta entrega usou JDK 25.0.3 e `--release 17`; não usa recursos de linguagem posteriores a Java 17.

## 2. Abrir no IntelliJ IDEA

1. Use **File > Open** e selecione `Projetos/Livraria/biblioteca-gof/pom.xml`.
2. Abra como projeto Maven e aguarde a resolução das dependências.
3. Configure um Project SDK Java 17+ e o mesmo JDK para o Maven Runner.
4. No terminal integrado, confira se está na pasta que contém esse `pom.xml`.
5. Para executar pela IDE, abra `src/main/java/br/pucpr/bcc/biblioteca/web/ConsoleApp.java` e execute `main`. Nas opções de execução, informe `demo` como argumento para a demonstração automática, ou `menu memoria B` para menu com banco B simulado.

## 3. Abrir no VS Code

1. Abra a pasta `Projetos/Livraria/biblioteca-gof`.
2. Instale o suporte Java/Maven da Microsoft, se ainda não estiver disponível.
3. Configure um JDK 17+ e aguarde a importação Maven.
4. Execute os comandos deste tutorial no terminal integrado ou execute `ConsoleApp.main` com os argumentos desejados.

O VS Code não foi operado nesta validação; a execução reproduzível foi feita pelo Maven. Não é necessário configurar servidor MySQL para os testes padrão.

## 4. Entrar na pasta correta

Partindo da raiz do repositório clonado:

```powershell
cd Projetos/Livraria/biblioteca-gof
```

Se o caminho absoluto contiver espaços, coloque-o entre aspas. Confirme que `pom.xml`, `schema.sql` e `README.md` estão na pasta atual.

Também é possível chamar a partir da raiz sem mudar de diretório:

```powershell
mvn -f Projetos/Livraria/biblioteca-gof/pom.xml clean test
```

Os comandos a seguir pressupõem a pasta do módulo. As aspas ao redor de `"-Dexec.args=..."` funcionam no PowerShell e em shells usuais.

## 5. Compilar e executar testes

```powershell
mvn clean compile
mvn clean test
```

`clean` limpa apenas a saída de compilação `target/`; não limpa o banco. `compile` compila a aplicação. `test` compila e executa JUnit, sem solicitar conexão MySQL. O H2 usado pelos testes JDBC é criado em memória e baixado automaticamente pelo Maven.

Confira `BUILD SUCCESS` e o resumo de testes com zero falhas e zero erros. Os detalhes ficam em `target/surefire-reports/`, que não é versionado.

Para executar somente os dez cenários originais:

```powershell
mvn test "-Dtest=RequisitosGoFTest"
```

## 6. Menu e demonstração automática

Menu padrão, em memória, banco A simulado:

```powershell
mvn clean compile exec:java
```

Demonstração automática em processo novo:

```powershell
mvn clean compile exec:java "-Dexec.args=demo"
```

Resultado esperado: reserva aceita; empréstimo com vencimento em 27/11/2026; renovação até 27/12/2026; devolução simulada em 01/01/2027; boleto de **R$ 5,00**. O livro permanece DEVOLVIDO até inspeção. As datas são fixas para tornar a demonstração reproduzível.

A demo usa IDs fixos. Execute-a em um processo novo em memória; repetir a opção 1 no mesmo menu não reinicia o acervo. Não execute a demo com dados reais como forma de limpeza.

## 7. Modos e argumentos

```powershell
mvn compile exec:java "-Dexec.args=menu memoria A"
mvn compile exec:java "-Dexec.args=menu memoria B"
mvn compile exec:java "-Dexec.args=menu prod A"
```

| Posição | Significado | Valores |
|---|---|---|
| 1 | Modo | `demo`; `menu` para uso interativo |
| 2 | Família de persistência | `memoria` ou `test`; `prod` ou `mysql` |
| 3 | Banco simulado | `A` ou `B` |

Os padrões, quando omitidos, são menu, memória e A. A família é escolhida uma vez por `ProvedorPersistencia.criarPorPerfil`. Os bancos A/B continuam simulados mesmo em `prod`; `prod` altera a persistência, não habilita um banco financeiro real.

`memoria` e `prod` são argumentos da aplicação, não perfis Maven. O perfil Maven `-Pmysql-it` é uma ferramenta separada para homologação.

## 8. Preparar o MySQL sem apagar dados

Para a instalação padrão, abra `schema.sql` no MySQL Workbench e execute-o na instância pretendida. Ele cria `biblioteca_ib2b` e tabelas com `IF NOT EXISTS`; não contém `DROP`, `TRUNCATE` nem remoção de dados.

Alternativamente, abra o cliente MySQL na pasta do módulo:

```powershell
mysql -h 127.0.0.1 -P 3306 -u root -p
```

No prompt do MySQL:

```sql
SOURCE schema.sql;
```

Substitua `root` pelo usuário autorizado. A opção `-p` solicita a senha interativamente. Se o executável não estiver no PATH, use o caminho completo da instalação MySQL.

**Base existente:** `IF NOT EXISTS` não atualiza a estrutura de tabelas já criadas. Faça backup e revise a estrutura antes de uma migração. As novas chaves estrangeiras não corrigem automaticamente registros órfãos ou empréstimos duplicados de versões anteriores. Não há migração destrutiva automática nesta entrega.

**Outra base:** as duas primeiras instruções de `schema.sql` selecionam explicitamente `biblioteca_ib2b`. Não basta trocar `DB_URL`. Para outra base, use uma cópia do script e ajuste `CREATE DATABASE` e `USE`, ou execute somente os blocos `CREATE TABLE` após selecionar a base correta. O teste de integração faz essa seleção de modo controlado.

## 9. Configurar a conexão da aplicação

No mesmo PowerShell onde executará o Maven:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/biblioteca_ib2b"
$env:DB_USER = "root"
$senhaMysql = Read-Host "Senha do MySQL" -AsSecureString
$env:DB_PASS = [System.Net.NetworkCredential]::new("", $senhaMysql).Password
mvn compile exec:java "-Dexec.args=menu prod A"
```

Essas variáveis valem para esse terminal e seus processos filhos. Não coloque senha no código, no tutorial, em `pom.xml` nem no Git. Se preferir variáveis persistentes do Windows, abra um terminal novo para que sejam herdadas.

Padrões da aplicação: URL `jdbc:mysql://localhost:3306/biblioteca_ib2b`, usuário `root`, senha vazia. Eles não garantem acesso à sua instalação. A aplicação não escreve no Oracle; configuração de réplica pertence à infraestrutura.

## 10. Homologação opcional com MySQL real

Prepare uma base exclusiva, sem substituir sua base de uso:

```sql
CREATE DATABASE IF NOT EXISTS biblioteca_gof_validacao
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configure a URL de validação, mantendo usuário e senha definidos no terminal:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/biblioteca_gof_validacao"
mvn -Pmysql-it verify
```

`MysqlIntegracaoIT` recusa outra base e outro SGBD. Cria apenas tabelas ausentes, usa identificadores de teste novos e **não apaga os registros de validação**. Verifica família/Connection, fluxo completo, terceira renovação recusada, boleto recuperável, uma linha por empréstimo, rollback, sete estados, integridade referencial e visibilidade em outra conexão. O usuário do banco precisa de permissões para criar tabelas, consultar e gravar nessa base de validação.

Além de `target/surefire-reports`, confira `target/failsafe-reports`. Para considerar a homologação aprovada, o teste de integração deve ter sido executado com sucesso, não simplesmente omitido.

## 11. Fluxo manual completo

Inicie `menu memoria A` e siga:

1. **Opção 2:** informe CPF fictício com 11 dígitos (por exemplo `11122233344`), nome, sobrenome, ID estudante `101`, matrícula `MAT-101`, ID livro `1` e título `Design Patterns`.
2. **Opção 3:** informe livro `1` e estudante `101`. O comprovante deve indicar `sucesso=true` e `RESERVADO`.
3. **Opção 4:** informe os mesmos IDs. O vencimento fica sete dias após hoje e o estado passa a `EMPRESTADO`.
4. **Opção 5:** informe os mesmos IDs. A renovação acrescenta 30 dias ao vencimento, passando a `RENOVADO`. A segunda renovação também é permitida; a terceira deve retornar `sucesso=false` sem aumentar a data.
5. **Opção 6:** informe os mesmos IDs. Se fez **uma renovação**, informe `42` dias após hoje; se fez **duas**, informe `72`. Isso simula cinco dias de atraso e gera boleto de R$ 5,00 na política original.
6. **Opção 8:** consulte livro `1`; deve estar `DEVOLVIDO`. Uma tentativa de reserva antes da inspeção deve ser recusada.
7. **Opção 7:** livro `1`, decisão `1` para bom estado: volta a `LIVRE`. Em uma devolução diferente, decisão `2` leva a `REPARANDO`; depois decisão `3` aplica laudo irrecuperável e leva a `INCINERADO`.
8. **Opção 9:** escolha `original`, `teto` ou `tolerancia` para as próximas emissões. A tolerância cobra R$ 3,00 para cinco dias de atraso. Boletos já emitidos conservam valor/política.
9. **Opção 0:** encerre o menu e feche a conexão da fábrica.

Se uma operação falhar, confira o comprovante antes de continuar. A opção 6 também solicita emissão de boleto; evite repeti-la para a mesma devolução, pois emissão única durável não faz parte do fluxo implementado. Alterações de política são administrativas locais; não há autenticação de perfis de usuário nesta CLI acadêmica.

## 12. Verificações no banco

No MySQL Workbench, selecione a base utilizada e consulte:

```sql
SELECT id_livro, estado, num_renovacoes, excluido FROM livros;
SELECT id_livro, id_estudante, ativa FROM reservas;
SELECT id_emprestimo, id_livro, data_vencimento, estado, devolvido FROM emprestimos;
SELECT id_livro, data_devolucao, data_vencimento FROM devolucoes;
SELECT id_cobranca, id_livro, valor, politica_aplicada, situacao FROM boletos_multa;
```

Após efetivar o empréstimo, a reserva correspondente foi removida. Renovar atualiza a mesma linha. Após devolução, `devolvido=1`, o livro está DEVOLVIDO e existe registro de devolução. O valor do boleto deve refletir a política vigente na emissão.

## 13. Problemas comuns

| Sintoma | Ação |
|---|---|
| `mvn` não reconhecido | Instalar/configurar Maven no PATH; abrir outro terminal; confirmar `mvn -version` |
| `release version 17 not supported` | Corrigir o JDK usado pelo Maven, não somente o Project SDK da IDE |
| `There is no POM in this directory` | Entrar em `Projetos/Livraria/biblioteca-gof` ou utilizar `-f` |
| Erro de resolução de plugins/dependências | Verificar acesso ao Maven Central e configuração de rede; executar antes da apresentação |
| `Access denied` | Conferir DB_USER, DB_PASS e permissões do usuário para a base; não alterar autenticação global para contornar |
| `Unknown database` ou tabela ausente | Criar a base correta e executar os blocos de schema correspondentes |
| `Communications link failure` | Conferir serviço MySQL, host, porta e URL; o modo memória não precisa desse serviço |
| Erro de FK | Cadastrar Pessoa antes de Estudante; cadastrar Livro/Estudante antes da movimentação; não remover entidades com histórico |
| Reserva recusada após devolução | Inspecionar pela opção 7 antes de reservar novamente |
| Demo não gera multa | Usar o comando documentado em processo novo; a versão atual simula 42 dias após o início |
| Texto acentuado aparece incorreto no terminal | Utilizar terminal UTF-8; no Windows, testar `chcp 65001` antes da execução |
| Mensagens de falha SMTP/rollback durante testes | São falhas injetadas pelos testes; confira o resumo JUnit e `BUILD SUCCESS` |

Os modelos da equipe ficam em `docs/modelagem/` (PDFs) e `docs/uml/originais/` (Astah). A correspondência entre os nomes do enunciado e os nomes das modelagens está em `docs/CORRESPONDENCIA_NOMES.md`. Os resultados finais e limites estão no README principal.
