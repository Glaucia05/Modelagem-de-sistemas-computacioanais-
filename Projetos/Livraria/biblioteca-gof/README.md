# Sistema de Gestão de Biblioteca — Padrões GoF (PUCPR)

Implementação em Java 17 (Maven) do sistema de cadastro, reserva, empréstimo, renovação e devolução de livros, aplicando os 10 padrões GoF exigidos. Os modelos originais foram preservados e a revisão da implementação está disponível em Astah e PDF em `docs/uml/`. Equipe: (Glaucia Bispo, Nicolly Stefany e Sérgio Calazans).

Consulte o [tutorial completo de execução](TUTORIAL_EXECUCAO.md) e a [procedência e reconciliação dos modelos UML](docs/uml/README.md).

## Requisitos de Ambiente
- Java 17 ou superior
- Maven 3.8+
- MySQL 8+ (opcional — necessário apenas se executar o perfil `prod` com banco ativo; o perfil de testes `test` usa a família em memória da `Abstract Factory`).

## Como Compilar e Executar os Testes (Comando Único)
```bash
mvn clean test
```

## Como Executar a Demonstração via CLI
```bash
mvn clean compile exec:java -Dexec.args="demo"
```
Para abrir o menu interativo completo:
```bash
mvn clean compile exec:java
```

## Configuração de Banco de Dados (Perfil `prod`)
1. Para uma instalação nova, execute `schema.sql` no MySQL principal. Não há conexão nem escrita Oracle no código; configurar e operar a réplica é responsabilidade da infraestrutura. Em bases existentes, `CREATE TABLE IF NOT EXISTS` não aplica novas restrições: a migração deve ser planejada após verificar os dados, sem apagar o histórico.
2. Defina as variáveis de ambiente (ou use os valores padrão):
   - `DB_URL` (padrão: `jdbc:mysql://localhost:3306/biblioteca_ib2b`)
   - `DB_USER` (padrão: `root`)
   - `DB_PASS` (padrão: `""`)

## Tabela de Rastreabilidade (Requisitos x Padrões GoF x Classes x Testes)

| Requisito | Padrão GoF | Papéis e Classes Principais | Cenário de Teste JUnit |
| :--- | :--- | :--- | :--- |
| **R01** Ciclo de vida do livro | **State** | Contexto: `Livro`; Interface: `EstadoLivro`; Estados: `LivreState`, `ReservadoState`, `EmprestadoState`, `RenovadoState`, `DevolvidoState`, `ReparandoState`, `IncineradoState` | `R01_terceiraRenovacaoRecusadaEDevolvidoNaoInspercionadoNaoReserva` |
| **R02** Políticas de cálculo de multa | **Strategy** | Estratégia: `PoliticaMulta`; Concretas: `MultaOriginalStrategy`, `MultaTetoVinteStrategy`, `MultaToleranciaDoisDiasStrategy`; Contexto: `ConfiguracaoMulta` | `R02_politicasMultaCincoDiasESemAtraso` |
| **R03** Integração com bancos incompatíveis | **Adapter** | Target: `CobrancaInstitucional`; Adaptees: `BancoASimulado`, `BancoBSimulado`; Adapters: `BancoAAdapter`, `BancoBAdapter` | `R03_adaptadorBancosAebESemDuplaQuitacao` |
| **R04** Entrada única para circulação | **Facade** | Fachada: `CirculacaoFacade`; Subsistemas: `RepositorioRelacionamento`, `RepositorioMovimentacao`, `RepositorioLivro`, `Livro`, `UnidadeTrabalho` | `R04_fachadaEfetivaEmprestimoRemovendoReservaSemExporColecoes` |
| **R05** Reações independentes à devolução | **Observer** | Subject/Publisher: `PublicadorDevolucao`; Evento: `EventoDevolucao`; Observer: `OuvinteDevolucao`; Concretos: `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail` | `R05_observadoresDevolucaoAtrasadaENoPrazo` |
| **R06** Solicitações de circulação como objetos | **Command** | Command: `ComandoCirculacao`; Concretos: `ReservarLivroCommand`, `RenovarEmprestimoCommand`; Invoker/Idempotência: `ExecutorComando` | `R06_comandoRenovacaoIdempotenteMesmoId` |
| **R07** Rotina comum para cadastros | **Template Method** | Classe Abstrata: `RotinaCadastroTemplate`; Concretas: `CadastroPessoaTemplate`, `CadastroEstudanteTemplate`, `CadastroAtendenteTemplate`, `CadastroLivroTemplate`; Interfaces: `iLivro`, `iPessoa`, `iEstudante`, `iAtendente` | `R07_templateMethodEstudanteDuplicadoECpfInexistente` |
| **R08** Famílias compatíveis de acesso aos dados | **Abstract Factory** | Fábrica Abstrata: `PersistenciaFactory`; Concretas: `MemoriaPersistenciaFactory`, `MysqlPersistenciaFactory` (JDBC real com `Connection` compartilhada na `MysqlUnidadeTrabalho`); Produtos: `UnidadeTrabalho`, `RepositorioLivro`, `RepositorioMovimentacao`, `RepositorioRelacionamento` | `R08_abstractFactoryFamiliasMemoriaEMysql` |
| **R09** Serviço de consulta com recursos opcionais | **Decorator** | Componente: `ConsultaAcervoService`; Base: `ConsultaAcervoBase`; Decorators: `ConsultaAcervoDecorator`, `MedicaoConsultaDecorator`, `CacheMetadadosDecorator` | `R09_decoratorCacheMetadadosMedicaoEEstadoAtualNaReserva` |
| **R10** Organização hierárquica do acervo | **Composite** | Component: `ElementoAcervo`; Leaf: `ItemLivroComposite`; Composite: `GrupoAcervoComposite` | `R10_compositeHierarquiaContagemEMoverSemAlterarLivro` |

## Uso de LLMs
Gemini (Google) apoiou a implementação inicial; Codex (OpenAI, família GPT-6 indicada pelo ambiente) apoiou a auditoria, correções, testes de regressão e revisão documental de 21/11/2026. Consulte `REGISTRO_LLM.md` para os pedidos e decisões, incluindo os registros anteriores da equipe.


## Auditoria técnica de 21/11/2026

### A. Diagnóstico e execução

O enunciado `Trabalho_Implementacao_GoF.pdf` foi lido integralmente (5 páginas). Foram inspecionados os 73 arquivos Java de produção, o teste original, `pom.xml`, `schema.sql`, README e registro de LLM. O Astah original foi aberto para leitura pela API instalada, sem salvar alterações.

| Etapa executada | Executados | Aprovados | Falhas de asserção | Erros | Ignorados |
|---|---:|---:|---:|---:|---:|
| Código original, `clean test` | 10 | 8 | 1 (R09) | 1 (R10) | 0 |
| Regressões iniciais contra código original | 21 | 13 | 6 | 2 | 0 |
| Após o primeiro lote (Command, Composite, State e composição R09) | 21 | 18 | 3 | 0 | 0 |
| Após transações e reconstituição JDBC | 21 | 21 | 0 | 0 | 0 |
| Regressões adicionais de integração e falhas | 29 | 28 | 0 | 1 | 0 |
| Após ajuste da restauração da conexão | 29 | 29 | 0 | 0 | 0 |
| Após integridade referencial e regressão da demonstração | 32 | 32 | 0 | 0 | 0 |
| Finalização: IDs bancários únicos entre instâncias | 33 | 33 | 0 | 0 | 0 |
| Integração adicional com MySQL 8.0.43 | 1 | 1 | 0 | 0 | 0 |

O erro intermediário na bateria de 29 testes ocorreu na restauração do `autoCommit` com conexão substituída por um proxy de teste; o código passou a usar o mesmo acesso `getConnection()` empregado no início/commit/rollback. A falha não foi ocultada alterando a expectativa do teste.

Ambiente efetivamente utilizado: Windows, JDK JetBrains Runtime 25.0.3 com compilação `--release 17`, Maven incluído no IntelliJ. O `mvn` não estava no PATH, e o Java encontrado inicialmente no PATH era antigo. Para repetir os comandos simples, configure Java 17+ e Maven no PATH e execute dentro de `Projetos/Livraria/biblioteca-gof`. Não foi afirmada uma execução em JVM 17 exata. O sandbox do Codex estava indisponível; as execuções foram realizadas com a permissão externa solicitada pela ferramenta.

Os testes de domínio usam a família em memória. `PersistenciaJdbcTest` executa SQL real da família JDBC contra H2 em modo MySQL, dependência exclusivamente de teste; não exige servidor nem configuração manual. Na finalização, `mvn -Pmysql-it verify` também executou `MysqlIntegracaoIT` contra MySQL **8.0.43** na base exclusiva `biblioteca_gof_validacao`: fluxo, commit visível em outra conexão, rollback, sete estados e integridade referencial. A homologação não mede carga nem concorrência entre processos.

A demonstração `mvn clean compile exec:java "-Dexec.args=demo"` terminou com sucesso: início em 20/11/2026, vencimento inicial em 27/11/2026, vencimento renovado em 27/12/2026 e devolução em 01/01/2027, com multa de R$ 5,00. As datas futuras são dados determinísticos da simulação.

### B. Matriz de conformidade

ATENDIDO nesta matriz significa implementação e cenário verificados no escopo acadêmico local; não equivale a homologação em produção nem elimina as pendências UML da seção D.

| Requisito | Padrão | Situação | Evidência executada | Correção realizada |
|---|---|---|---|---|
| R01 | State | ATENDIDO | Teste original R01; `R01_livroExcluidoNaoPodeReservar`; `R01_R04_datasInvalidasEEventosDeOutroLivroNaoAlteramDominio`; `R01_R08_reconstituiTodosEstadosEListagemCompleta` | Bloqueio de reserva após exclusão; validação do empréstimo recebido; recuperação dos sete estados e atributos JDBC |
| R02 | Strategy | ATENDIDO | R02 original; `R02_multaEmitidaPreservaValorEPolitica`; `R02_demoCompletaEmiteCincoReaisAposRenovacao` | Captura de uma única política por emissão; seleção administrativa no menu; correção da data da demonstração |
| R03 | Adapter | ATENDIDO | R03 original; `R03_converteDocumentosERejeitaQuitacaoDesconhecidaOuValorDivergente` | CPF formatado no B; validação de cobrança e valor; quitação simultânea aceita uma única vez; UUID evita reutilização de IDs entre instâncias |
| R04 | Facade | ATENDIDO | R04 original; rollback após gravação parcial; falha ao iniciar; devolução revertida sem evento | Snapshot em memória, cópias das entidades, coordenação no monitor da unidade, início protegido e rollback de operações próprias |
| R05 | Observer | ATENDIDO | R05 original (atraso); `R05_noPrazoEmailFalhaAntesDosDemaisEPublicacaoAposCommit`; `R05_rollbackDaDevolucaoNaoPublicaEvento` | Reforço dos testes e diagnóstico das falhas isoladas; publicação permanece após commit |
| R06 | Command | ATENDIDO | R06 original; `R06_submissoesSimultaneasRecebemMesmoResultadoESomamSoTrintaDias` | Execução e armazenamento atômicos no executor; repetição aguarda o resultado original |
| R07 | Template Method | ATENDIDO | R07 original; `R07_ordemDoTemplateFinalEValidacaoSemGravacao`; cadastro com pessoa referenciada no JDBC | CPF nulo chega à validação; sobrenome não nulo, em conformidade com SQL; método final e ordem preservados |
| R08 | Abstract Factory | ATENDIDO | R08 original; 7 testes JDBC; regressões em memória; `MysqlIntegracaoIT` com MySQL 8.0.43 | Atualização por ID persistido, transação/conexão compartilhada, fechamento, snapshots, restrições referenciais e homologação real |
| R09 | Decorator | ATENDIDO | R09 original corrigido; `R09_cacheHitMedidoSemReconsultarMetadadosEFalhaTemFallback` | Cadeia `Medição(Cache(Base))`; duas medições com uma consulta de metadados à base; estado atual consultado separadamente |
| R10 | Composite | ATENDIDO | R10 original; `R10_recusaCiclosDuploPaiDuplicacaoETransferenciaInvalida` | Grupos montados não registram novamente suas folhas; controle de pai, ciclos e transferência validada |

R05 original tinha nome mencionando devolução no prazo, mas só executava atraso. R06 original só verificava repetição sequencial. R08 original verificava os tipos/identidade dos objetos, sem executar SQL. Os novos testes verificam comportamento além da existência das classes. A tabela de rastreabilidade anterior identifica os papéis GoF; esta matriz acrescenta as evidências de regressão.

### C. Alterações, causas e gravidade

Os caminhos de produção abaixo são relativos a `src/main/java/br/pucpr/bcc/biblioteca/`. Os testes citados estão em `src/test/java/br/pucpr/bcc/biblioteca/`.

| Gravidade original | Arquivo e método | Problema e causa | Solução | Evidência |
|---|---|---|---|---|
| CRÍTICO | `r08_abstract_factory/MemoriaPersistenciaFactory`, `MemoriaUnidadeTrabalho.iniciar/rollback` e repositórios | Rollback apenas desligava uma flag; objetos compartilhados já estavam modificados | Snapshots das coleções, cópias na entrada/saída, restauração e monitor comum | `R04_rollbackRestauraLivroReservaEEmprestimoAposFalhaIntermediaria`, `R05_rollbackDaDevolucaoNaoPublicaEvento` |
| CRÍTICO | `dominio/Emprestimo` e `MysqlRepositorioMovimentacao.salvarEmprestimo/buscarEmprestimoAtivo` | Renovar/devolver inseria nova linha, mantendo empréstimos antigos ativos; estado era perdido na leitura | ID persistido, chaves geradas, UPDATE da mesma linha e reconstituição da situação | `R08_renovacaoAtualizaLinhaEDevolucaoEncerraEmprestimo` |
| ALTO | `r10_composite/GrupoAcervoComposite.adicionar/moverItemPara` | Anexar seção já povoada duplicava registro global; grupos podiam ter dois pais; destino inválido removia antes de validar | Registro só das folhas, pai único, ciclos e destino conferidos antes de mover | R10 original e regressão R10 |
| ALTO | `RequisitosGoFTest.R09_decoratorCacheMetadadosMedicaoEEstadoAtualNaReserva` | Cache externo encerrava a chamada sem alcançar a medição | Ordem correta de composição, expectativa exata de 2 medições; nenhuma expectativa foi afrouxada | R09 original e regressão com contador de acessos à base |
| ALTO | `r06_command/ExecutorComando.executar` | ID era marcado antes do resultado; segunda submissão recebia falso insucesso e exceção deixava ID consumido | Uma seção crítica reúne execução e resultado; exceções não inserem resultado no mapa | Regressão R06 concorrente, incluindo limite de duas renovações |
| ALTO | `ui/ConsoleApp.executarFluxoCompletoDemonstracao` | Devolução no dia 12 não era atrasada após renovação até o dia 37 | Devolução no dia 42 | `R02_demoCompletaEmiteCincoReaisAposRenovacao` e execução CLI |
| ALTO | `r01_state/LivreState.reservar`, `ConsultaAcervoBase.podeReservar` | Exclusão lógica preservava LIVRE e permitia nova reserva | Conferência de `excluido` nas duas entradas | `R01_livroExcluidoNaoPodeReservar` |
| ALTO | `schema.sql`, família em memória | Registros órfãos e remoção de entidades referenciadas eram possíveis | FKs de movimentações no schema novo; validação equivalente nas famílias criadas em memória | `R08_integridadeReferencialImpedeOrfaos`, `R08_memoriaImpedeOrfaosComoFamiliaRelacional` |
| MÉDIO | `r03_adapter/BancoAAdapter/BancoBAdapter.registrarCobranca/processarNotificacao`, bancos simulados | B não formatava CPF; notificações desconhecidas ou com valor divergente quitavam; geração de IDs concorrente não era protegida | Conversão, conferência da cobrança emitida, operação atômica de quitação e emissão sincronizada | Regressão R03, com duas notificações simultâneas no B |
| MÉDIO | `r04_facade/CirculacaoFacade`, métodos transacionais | Falha de início escapava do comprovante; rollback podia esconder erro original ou desfazer transação externa | Início dentro do tratamento; controle da transação iniciada; diagnóstico de falhas adicionais; serialização pela unidade | Falha ao iniciar, rollback intermediário e cenários R04/R05 |
| MÉDIO | `MysqlUnidadeTrabalho.iniciar/commit/rollback/close`, `PersistenciaFactory.close`, `ConsoleApp.main` | Flag ativa incorreta na falha de conexão/commit; conexão sem fechamento definido | Flag acompanha o sucesso real, commit falho permite rollback, descarte em falhas e fechamento ao encerrar a aplicação | `R08_falhaCommitPermiteRollbackERetentativa`, `R08_conexaoInvalidaNaoMarcaTransacaoAtiva`, rollback/commit H2 |
| MÉDIO | `MysqlRepositorioLivro.listarTodos` | Reserva e exclusão eram omitidas na reconstituição da lista | Mesmos atributos restaurados na busca e listagem | `R01_R08_reconstituiTodosEstadosEListagemCompleta` |
| MÉDIO | `MysqlRepositorioRelacionamento.salvarPessoa` e demais upserts | `REPLACE` tem semântica de remoção/inserção no MySQL e conflita com referências | `INSERT ... ON DUPLICATE KEY UPDATE`, sem apagar identidade/histórico | `R07_R08_atualizarPessoaReferenciadaNaoExecutaDeleteImplicito`; SQL inspecionado, execução no H2 |
| MÉDIO | `TratadorDeDevolucao.emitirBoletoMulta/definirPoliticaMulta`, `ConfiguracaoMulta` | Duas leituras de política podiam associar nome/valor diferentes; ausência de estudante gerava CPF fictício; seleção só por código | Snapshot da política, falha explícita para estudante ausente e configuração via tratador/menu | Regressão R02 e inspeção da delegação UI |
| MÉDIO | `EmprestadoState/RenovadoState.renovar/devolver`, `ReservadoState.emprestar`, fachada | Eventos com empréstimo de outro livro e datas inválidas podiam modificar objetos | Guardas antes da mutação e validação cronológica | `R01_R04_datasInvalidasEEventosDeOutroLivroNaoAlteramDominio` |
| BAIXO | `CadastroPessoaTemplate.buscarExistente/validar` | CPF nulo causava NPE no mapa antes de validar; sobrenome nulo divergia do SQL | Busca sem chave nula e validação antes da gravação | Regressão R07 |
| BAIXO | `PublicadorDevolucao.publicar`, `pom.xml`, `.gitignore` | Falhas dos ouvintes silenciosas; ausência de teste JDBC; build aparecia como arquivo não rastreado | Diagnóstico isolado, H2 somente em testes e exclusão de `target/` do versionamento | R05, suíte JDBC e `git diff --check` |

O conjunto agrupa **2 problemas críticos, 6 altos, 7 médios e 2 baixos corrigidos**. A pendência UML, de gravidade alta para a entrega, é adicional. São grupos de causas, não uma contagem de linhas ou de arquivos.

### Arquitetura preservada e limites das decisões

Fluxo operacional: `ConsoleApp -> interfaces/tratadores -> CirculacaoFacade -> Livro/EstadoLivro + repositórios + unidade de trabalho`; depois do commit da devolução, o publicador chama os ouvintes. Cadastros continuam no Template Method. A composição das dependências acontece em `main`; as ações do menu chamam tratadores e não coleções dos repositórios.

`RepositorioLivro` representa o acesso ao Acervo, `RepositorioRelacionamento` o cadastro de pessoas/papéis, e `RepositorioMovimentacao` reservas, empréstimos, devoluções e boletos. Esses papéis existem, mas os nomes e certas assinaturas diferem do Astah original. Não foram criadas fachadas vazias com os nomes antigos apenas para aparentar equivalência UML. A dependência do domínio em State e dos estados em Livro é própria do padrão; não foi encontrado ciclo entre repositórios e UI.

A fachada valida a identidade da unidade de trabalho dos repositórios concretos. O método default `unidadeTrabalho()` conserva compatibilidade de implementações/decoradores antigos; ao criar um novo wrapper, encaminhe esse método ao repositório interno para manter a verificação. Construtores públicos foram preservados. Os repositórios devem ser obtidos pela fábrica para terem transação e integridade coordenadas.

A unidade em memória usa snapshots e cópias de entidades mutáveis. O custo é proporcional ao volume dos dados, aceitável neste projeto acadêmico; não é uma solução para grande escala. O executor sincroniza os comandos da instância e guarda seus resultados em memória. A consulta JDBC de Livro utiliza `FOR UPDATE` dentro da transação; o fluxo e as transações foram validados em MySQL real; carga e concorrência entre processos não foram homologadas.

### D. Pendências e limitações

1. **UML reconciliada em revisão identificada.** `docs/uml/originais/` contém cópia integral do Astah fornecido. `Biblioteca-GoF.asta` conserva os pacotes históricos e acrescenta dez diagramas de classes, dez sequências e a máquina de estados atual. `Diagramas-GoF.pdf` reúne os 21 diagramas atuais exportados pelo Astah. As operações são resumidas; a equipe deve revisar a semântica e a apresentação para a arguição. Os diagramas históricos continuam identificados como históricos, não como implementação atual.
2. **Diferenças documentadas.** Guarda de renovação corrigida de `<=2` para `<2` na revisão; cadastro reunido em `TratadorDeCadastro`; repositórios concretizam os papéis Acervo, Relacionamento e Movimentação. IDs e classes seguem o código, sem inventar ISBN/autor ou a superclasse Transacao ausentes. Consulte a reconciliação em `docs/uml/README.md`.
3. **MySQL real homologado no cenário de integração.** Foram usadas credenciais locais por ambiente, sem versioná-las. O teste cria apenas tabelas ausentes na base de validação e mantém seus registros. Bases antigas ainda exigem análise de duplicatas/órfãos antes de migração; nenhuma exclusão automática foi executada.
4. **MÉDIA — persistência da idempotência.** Resultados de comandos e deduplicação de notificações duram a instância do executor/adaptador. Reiniciar ou distribuir a aplicação exige armazenamento persistente de chaves e política de retenção. Bancos/SMTP são simulações, como permitido pelo enunciado; a quitação traduzida pelo adaptador não constitui um webhook financeiro persistente de ponta a ponta. A emissão repetida de boleto para a mesma devolução também não possui uma chave persistente de emissão única. São limites para uso real, não evidência de integração bancária real.
5. **MÉDIA — efeitos externos.** Emissão bancária seguida de falha ao gravar boleto exige reconciliação; banco externo e JDBC não compartilham transação. Filas dos ouvintes são locais e não há retentativa durável de e-mail. Uma solução de outbox e conciliação mudaria a arquitetura; foi registrada como evolução, sem introduzir framework ou transação distribuída nesta entrega.
6. **BAIXA — repetição da demo na mesma sessão.** A demonstração usa IDs determinísticos e não restaura automaticamente o acervo. Execute `demo` em um novo processo em memória. Repetir a opção 1 do menu com o livro ainda DEVOLVIDO pode apresentar recusas; não apague dados para contornar isso.
7. **BAIXA — cache.** Cache local guarda somente metadados e não tem TTL/invalidação após alteração do título. Pode apresentar título antigo, mas não autoriza reservas: o estado atual é buscado separadamente e a fachada aplica State. Cache e Composite são demonstrados pelos testes, sem tela específica de administração da hierarquia.

A finalização foi autorizada em `sergio-calazans/finalizacao-gof`, com commits por responsabilidade e publicação sem merge. O original Astah externo foi preservado; não houve alteração destrutiva de banco.

### E. Roteiro de demonstração e arguição

Execução dos modos e da configuração:

```bash
mvn clean test
mvn clean compile exec:java "-Dexec.args=demo"
mvn compile exec:java "-Dexec.args=menu memoria B"
mvn compile exec:java "-Dexec.args=menu prod A"
```

O primeiro argumento é o modo (`demo` ou `menu`), o segundo escolhe a fábrica (`memoria`/`test` ou `prod`/`mysql`) e o terceiro escolhe o adaptador (`A` ou `B`). Não são perfis Maven `-P`. Os padrões são menu, memória e banco A. As variáveis `DB_URL`, `DB_USER`, `DB_PASS` são lidas apenas pela família MySQL. Não há configuração Oracle de escrita. No menu, a opção 9 seleciona `original`, `teto` ou `tolerancia` pelo tratador.

Para os cinco minutos: reserve aproximadamente 1 minuto para os testes já com dependências baixadas, 1 minuto para a demo e 3 minutos para perguntas/extensão. Mostre a terceira renovação recusada, o empréstimo encerrado, DEVOLVIDO antes da inspeção e o R09 com cache hit medido. Antes da apresentação, execute uma vez para baixar dependências e confira `java -version` e `mvn -version`.

As perguntas de arguição são apresentadas somente na resposta de entrega, conforme solicitado. O conhecimento individual de cada integrante não foi avaliado.

### F. Avaliação estimada

| Critério do professor | Peso | Estimativa de pontos | Fundamentação e limite |
|---|---:|---:|---|
| Aplicação dos dez padrões | 40 | 40 | Dez cenários obrigatórios e regressões executados; famílias reais homologadas no cenário local |
| Cenários automatizados | 20 | 20 | 33 testes padrão mais 1 integração MySQL; zero falhas/erros |
| Coerência código/UML | 15 | 12 | Original preservado, revisão Astah validada e PDF; modelos resumidos ainda requerem revisão final da equipe |
| Responsabilidades e falhas | 15 | 14 | Tratadores, domínio, rollback, isolamento de ouvintes e idempotência concorrente; limites de durabilidade explícitos |
| Documentação | 10 | 10 | Tutorial, rastreabilidade, modelos e histórico das duas ferramentas |
| **Total estimado** | **100** | **96** | **Estimativa técnica, não garantia de nota** |

Os resultados de testes são evidências; a pontuação é julgamento estimativo. A arguição individual e a aceitação dos modelos pelo professor podem alterar a avaliação. Persistência durável de comandos, webhook financeiro e outbox não foram introduzidos porque o enunciado permite integrações simuladas e não exige implantação distribuída.

### Complemento da finalização — 21/11/2026

Foi identificado mais um defeito de gravidade **alta** em `BancoASimulado.criarBoletoCentavos` e `BancoBSimulado.gerarCobrancaDecimal`: contadores reiniciavam em cada instância e reutilizavam IDs de cobranças já persistidas. A solução mantém os prefixos BA/BB e utiliza UUID. A regressão `R03_novasInstanciasDoBancoNaoReutilizamIdentificadoresPersistidos` e a recuperação do boleto no teste MySQL verificam a correção. A suíte anterior de 32 testes foi preservada, chegando a 33 testes padrão.

As validações finais incluem compilação, demo automática com R$ 5,00 e CLI por entrada de terminal. O perfil em memória continua independente de servidor. Não foi realizada homologação bancária real, SMTP real, Oracle, carga distribuída ou execução em JVM 17 exata.
