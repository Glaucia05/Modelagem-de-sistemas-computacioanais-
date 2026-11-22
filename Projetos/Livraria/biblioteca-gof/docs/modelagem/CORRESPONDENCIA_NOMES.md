# Correspondência entre enunciado, modelagens e código

Os nomes do código seguem as modelagens da equipe (`docs/modelagem/`). Os padrões mantêm o papel no nome (`CirculacaoFacade`, `*Strategy`, `*Decorator`…), com vocabulário do domínio.

## 1. Pontos de acesso do enunciado

O enunciado cita três pontos internos de acesso. Cada um corresponde a classes da modelagem:

| Enunciado | Modelagem | Código (`persistencia`) |
|---|---|---|
| Acervo | `AcervoDeLivros` | `AcervoDeLivros`, `MemoriaAcervoDeLivros`, `MysqlAcervoDeLivros` |
| Movimentacao | `Movimentacao` | `Movimentacao`, `MemoriaMovimentacao`, `MysqlMovimentacao` |
| Relacionamento | `GrupoDePessoas`, `GrupoDeAcademicos`, `GrupoDeColaboradores` | as três interfaces, reunidas em `GruposDoSistema` (`MemoriaGrupos`, `MysqlGrupos`) |

## 2. Contratos de persistência (R08)

| Interface | Operações |
|---|---|
| `PersistenciaFactory` | `familia()`, `unidadeTrabalho()`, `acervoDeLivros()`, `movimentacao()`, `grupos()` |
| `AcervoDeLivros` | `inserirLivro` (insere ou substitui), `consultarLivro`, `listarLivros`, `excluirLivro` |
| `Movimentacao` | `inserirReserva`, `consultarReserva`, `removerReserva`, `inserirEmprestimo`, `consultarEmprestimo`, `inserirDevolucao`, `consultarDevolucao`, `salvarBoleto`, `listarBoletos` |
| `GrupoDePessoas` | `inserirPessoa` (insere ou substitui), `consultarPessoa`, `excluirPessoa` |
| `GrupoDeAcademicos` | `inserirEstudante` (insere ou substitui), `consultarEstudante`, `excluirEstudante` |
| `GrupoDeColaboradores` | `inserirAtendente` (insere ou substitui), `consultarAtendente`, `excluirAtendente` |

`salvarBoleto` e `listarBoletos` não aparecem na modelagem, que não detalha o boleto.

## 3. Componentes, interfaces e pacotes

Pacotes sob `br.pucpr.bcc.biblioteca`.

| Componente / interface | Código |
|---|---|
| Web | `web` (`ConsoleApp`) |
| Biblioteca | `interfaces` (`iLivro`, `iEmprestimo`, `iDevolucao`); `tratadores`; `dominio`; `estado`, `circulacao`, `eventos`, `comandos`, `consulta`, `acervo`, `cadastro`, `persistencia` |
| Acadêmico / Recursos Humanos | `iEstudante`, `iPessoa`, `iAtendente`, implementadas por `TratadorDeCadastro` |
| Financeiro | `financeiro.multa`, `financeiro.cobranca` |
| Banco (externo) | `banco` (`BancoASimulado`, `BancoBSimulado`) |
| `iMulta` | `financeiro.multa.iMulta` (contrato das políticas de multa) |
| `iCobranca`, `iQuitacao` | `financeiro.cobranca`; `CobrancaInstitucional` reúne as duas |

| Pacote | Padrão | Requisito |
|---|---|---|
| `estado` | State | R01 |
| `financeiro.multa` | Strategy | R02 |
| `financeiro.cobranca` | Adapter | R03 |
| `circulacao` | Facade | R04 |
| `eventos` | Observer | R05 |
| `comandos` | Command | R06 |
| `cadastro` | Template Method | R07 |
| `persistencia` | Abstract Factory | R08 |
| `consulta` | Decorator | R09 |
| `acervo` | Composite | R10 |

## 4. Rastreabilidade (padrão → papéis → teste)

Testes em `RequisitosGoFTest`.

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