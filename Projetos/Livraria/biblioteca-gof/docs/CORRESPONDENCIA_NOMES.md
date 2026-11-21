# Correspondência de nomes: enunciado GoF → modelagens da equipe

Regra adotada: valem os nomes das modelagens da equipe (`docs/modelagem/`). Os nomes do enunciado só permanecem onde a modelagem não tem equivalente. Os padrões mantêm o papel no nome (`CirculacaoFacade`, `*Strategy`, `*Decorator`…), com vocabulário do domínio.

## 1. Pontos de acesso e persistência (R08)

| Enunciado | Modelagem | Código |
|---|---|---|
| Acervo / repositório de Livro | `AcervoDeLivros` | `persistencia.AcervoDeLivros`, `MemoriaAcervoDeLivros`, `MysqlAcervoDeLivros` |
| Movimentacao / repositório | `Movimentacao` | `persistencia.Movimentacao`, `MemoriaMovimentacao`, `MysqlMovimentacao` |
| Relacionamento / repositório | `GrupoDePessoas`, `GrupoDeAcademicos`, `GrupoDeColaboradores` | `GrupoDePessoas`, `GrupoDeAcademicos`, `GrupoDeColaboradores`, reunidos em `GruposDoSistema` (`MemoriaGrupos`, `MysqlGrupos`) |
| `repositorioLivro()` / `repositorioMovimentacao()` / `repositorioRelacionamento()` | — | `acervoDeLivros()` / `movimentacao()` / `grupos()` |

Métodos de persistência renomeados para os da classe de projeto:

| Antes | Depois |
|---|---|
| `salvar`, `buscarPorId`, `listarTodos`, `remover` (livro) | `inserirLivro` (insere ou substitui), `consultarLivro`, `listarLivros`, `excluirLivro` |
| `salvarReserva`, `buscarReservaAtiva` | `inserirReserva`, `consultarReserva` (`removerReserva` mantido) |
| `salvarEmprestimo`, `buscarEmprestimoAtivo` | `inserirEmprestimo`, `consultarEmprestimo` |
| `salvarDevolucao`, `buscarDevolucao` | `inserirDevolucao`, `consultarDevolucao` |
| `salvarPessoa`, `buscarPessoaPorCpf`, `removerPessoa` | `inserirPessoa`, `consultarPessoa`, `excluirPessoa` |
| `salvarEstudante`, `buscarEstudantePorId`, `removerEstudante` | `inserirEstudante`, `consultarEstudante`, `excluirEstudante` |
| `salvarAtendente`, `buscarAtendentePorId`, `removerAtendente` | `inserirAtendente`, `consultarAtendente`, `excluirAtendente` |

`salvarBoleto` e `listarBoletos` ficam como estavam (a modelagem não detalha o boleto).

## 2. Componentes e interfaces (diagrama de componentes)

| Componente / interface | Código |
|---|---|
| Web | pacote `web` (`ConsoleApp`) |
| Biblioteca | `interfaces.iLivro`, `iEmprestimo`, `iDevolucao`; `tratadores.*`; pacotes `estado`, `circulacao`, `eventos`, `comandos`, `consulta`, `acervo`, `cadastro` |
| Acadêmico / Recursos Humanos | `iEstudante`, `iPessoa`, `iAtendente` (implementadas por `TratadorDeCadastro`) |
| Financeiro | pacotes `financeiro.multa` e `financeiro.cobranca` |
| Banco (externo) | pacote `banco` (`BancoASimulado`, `BancoBSimulado`) |
| `iMulta` | `financeiro.multa.iMulta` (antes `PoliticaMulta`; contrato das políticas de multa) |
| `iCobranca`, `iQuitacao` | `financeiro.cobranca.iCobranca`, `iQuitacao`; `CobrancaInstitucional` os reúne |

## 3. Pacotes

| Antes | Depois |
|---|---|
| `r01_state` | `estado` |
| `r02_strategy` | `financeiro.multa` |
| `r03_adapter` | `financeiro.cobranca` (simulados em `banco`) |
| `r04_facade` | `circulacao` |
| `r05_observer` | `eventos` |
| `r06_command` | `comandos` |
| `r07_template` | `cadastro` |
| `r08_abstract_factory` | `persistencia` |
| `r09_decorator` | `consulta` |
| `r10_composite` | `acervo` |
| `ui` | `web` |

## 4. Rastreabilidade (padrão → papéis → teste)

Pacotes abaixo sob `br.pucpr.bcc.biblioteca`. Testes em `RequisitosGoFTest`.

| Req. | Padrão | Papéis no código | Teste |
|---|---|---|---|
| 1 | State | contexto `dominio.Livro`; estado `EstadoLivro` / `AbstractEstadoLivro`; concretos `LivreState`, `ReservadoState`, `EmprestadoState`, `RenovadoState`, `DevolvidoState`, `ReparandoState`, `IncineradoState` | `R01_terceiraRenovacaoRecusadaEDevolvidoNaoInspercionadoNaoReserva` |
| 2 | Strategy | estratégia `iMulta`; concretas `MultaOriginalStrategy`, `MultaTetoVinteStrategy`, `MultaToleranciaDoisDiasStrategy`; configuração `ConfiguracaoMulta`; cliente `TratadorDeDevolucao` | `R02_politicasMultaCincoDiasESemAtraso` |
| 3 | Adapter | alvo `CobrancaInstitucional` (`iCobranca` + `iQuitacao`); adaptadores `BancoAAdapter`, `BancoBAdapter`; adaptados `BancoASimulado`, `BancoBSimulado` | `R03_adaptadorBancosAebESemDuplaQuitacao` |
| 4 | Facade | `CirculacaoFacade`, `ComprovanteOperacao`; clientes `TratadorDeEmprestimo`, `TratadorDeDevolucao` | `R04_fachadaEfetivaEmprestimoRemovendoReservaSemExporColecoes` |
| 5 | Observer | publicador `PublicadorDevolucao`; ouvinte `OuvinteDevolucao`; `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail`; evento `EventoDevolucao` | `R05_observadoresDevolucaoAtrasadaENoPrazo` |
| 6 | Command | comando `ComandoCirculacao`; `ReservarLivroCommand`, `RenovarEmprestimoCommand`; invocador `ExecutorComando`; receptor `CirculacaoFacade` | `R06_comandoRenovacaoIdempotenteMesmoId` |
| 7 | Template Method | `RotinaCadastroTemplate`; `CadastroLivroTemplate`, `CadastroPessoaTemplate`, `CadastroEstudanteTemplate`, `CadastroAtendenteTemplate` | `R07_templateMethodEstudanteDuplicadoECpfInexistente` |
| 8 | Abstract Factory | fábrica `PersistenciaFactory`; `MemoriaPersistenciaFactory`, `MysqlPersistenciaFactory`; seleção `ProvedorPersistencia`; produtos `UnidadeTrabalho`, `AcervoDeLivros`, `Movimentacao`, `GruposDoSistema` | `R08_abstractFactoryFamiliasMemoriaEMysql` |
| 9 | Decorator | componente `ConsultaAcervoService`; base `ConsultaAcervoBase`; `ConsultaAcervoDecorator`; `MedicaoConsultaDecorator`, `CacheMetadadosDecorator` | `R09_decoratorCacheMetadadosMedicaoEEstadoAtualNaReserva` |
| 10 | Composite | componente `ElementoAcervo`; composto `GrupoAcervoComposite`; folha `ItemLivroComposite` | `R10_compositeHierarquiaContagemEMoverSemAlterarLivro` |
