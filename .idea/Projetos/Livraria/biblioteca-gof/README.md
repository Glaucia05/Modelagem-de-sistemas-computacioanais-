# Sistema de Gestão de Biblioteca — Padrões GoF (PUCPR)

Implementação em Java 17 (Maven) do sistema de cadastro, reserva, empréstimo, renovação e devolução de livros, aplicando os 10 padrões GoF exigidos e mantendo coerência com os diagramas UML da equipe (Glaucia Bispo, Nicolly Stefany e Sérgio Calazans).

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
1. Execute o script `schema.sql` no servidor MySQL principal (`Dell Edge 3600 principal`). O servidor Oracle permanece apenas como réplica de leitura e não recebe escritas da aplicação.
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
Consulte o arquivo `REGISTRO_LLM.md` para detalhes das ferramentas e prompts utilizados.
