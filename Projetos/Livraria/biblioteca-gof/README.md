# Sistema de Gestão de Biblioteca — Padrões GoF (PUCPR)

Implementação em Java 17 (Maven) do sistema de cadastro, reserva, empréstimo, renovação e devolução de livros, aplicando os 10 padrões GoF exigidos. Os modelos originais foram preservados e a revisão da implementação está disponível em Astah e PDF em `docs/uml/`. Equipe: Glaucia Bispo, Nicolly Stefany e Sérgio Calazans.

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
mvn clean compile exec:java "-Dexec.args=demo"
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

| Requisito                                          | Padrão GoF           | Papéis e Classes Principais                                                                                                                                                                                                                                        | Cenário de Teste JUnit                                              |
| :------------------------------------------------- | :------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------ |
| **R01** Ciclo de vida do livro                     | **State**            | Contexto: `Livro`; Interface: `EstadoLivro`; Estados: `LivreState`, `ReservadoState`, `EmprestadoState`, `RenovadoState`, `DevolvidoState`, `ReparandoState`, `IncineradoState`                                                                                    | `R01_terceiraRenovacaoRecusadaEDevolvidoNaoInspercionadoNaoReserva` |
| **R02** Políticas de cálculo de multa              | **Strategy**         | Estratégia: `PoliticaMulta`; Concretas: `MultaOriginalStrategy`, `MultaTetoVinteStrategy`, `MultaToleranciaDoisDiasStrategy`; Contexto: `ConfiguracaoMulta`                                                                                                        | `R02_politicasMultaCincoDiasESemAtraso`                             |
| **R03** Integração com bancos incompatíveis        | **Adapter**          | Target: `CobrancaInstitucional`; Adaptees: `BancoASimulado`, `BancoBSimulado`; Adapters: `BancoAAdapter`, `BancoBAdapter`                                                                                                                                          | `R03_adaptadorBancosAebESemDuplaQuitacao`                           |
| **R04** Entrada única para circulação              | **Facade**           | Fachada: `CirculacaoFacade`; Subsistemas: `Relacionamento`, `Movimentacao`, `AcervoDeLivros`, `Livro`, `UnidadeTrabalho`                                                                                                                                           | `R04_fachadaEfetivaEmprestimoRemovendoReservaSemExporColecoes`      |
| **R05** Reações independentes à devolução          | **Observer**         | Subject/Publisher: `PublicadorDevolucao`; Evento: `EventoDevolucao`; Observer: `OuvinteDevolucao`; Concretos: `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail`                                                                              | `R05_observadoresDevolucaoAtrasadaENoPrazo`                         |
| **R06** Solicitações de circulação como objetos    | **Command**          | Command: `ComandoCirculacao`; Concretos: `ReservarLivroCommand`, `RenovarEmprestimoCommand`; Invoker/Idempotência: `ExecutorComando`                                                                                                                               | `R06_comandoRenovacaoIdempotenteMesmoId`                            |
| **R07** Rotina comum para cadastros                | **Template Method**  | Classe Abstrata: `RotinaCadastroTemplate`; Concretas: `CadastroPessoaTemplate`, `CadastroEstudanteTemplate`, `CadastroAtendenteTemplate`, `CadastroLivroTemplate`; Interfaces: `iLivro`, `iPessoa`, `iEstudante`, `iAtendente`                                     | `R07_templateMethodEstudanteDuplicadoECpfInexistente`               |
| **R08** Famílias compatíveis de acesso aos dados   | **Abstract Factory** | Fábrica Abstrata: `PersistenciaFactory`; Concretas: `MemoriaPersistenciaFactory`, `MysqlPersistenciaFactory` (JDBC real com `Connection` compartilhada na `MysqlUnidadeTrabalho`); Produtos: `UnidadeTrabalho`, `AcervoDeLivros`, `Movimentacao`, `Relacionamento` | `R08_abstractFactoryFamiliasMemoriaEMysql`                          |
| **R09** Serviço de consulta com recursos opcionais | **Decorator**        | Componente: `ConsultaAcervoService`; Base: `ConsultaAcervoBase`; Decorators: `ConsultaAcervoDecorator`, `MedicaoConsultaDecorator`, `CacheMetadadosDecorator`                                                                                                      | `R09_decoratorCacheMetadadosMedicaoEEstadoAtualNaReserva`           |
| **R10** Organização hierárquica do acervo          | **Composite**        | Component: `ElementoAcervo`; Leaf: `ItemLivroComposite`; Composite: `GrupoAcervoComposite`                                                                                                                                                                         | `R10_compositeHierarquiaContagemEMoverSemAlterarLivro`              |

## Uso de LLMs

Gemini (Google) apoiou a implementação inicial; Codex (OpenAI) apoiou a auditoria, correções, testes e documentação; Claude (Anthropic) apoiou o alinhamento dos nomes aos modelos UML e a conferência final; ChatGPT (OpenAI) apoiou a revisão final das pendências do enunciado. Consulte `REGISTRO_LLM.md` para os pedidos e decisões.

## Demonstração e verificação

- `mvn clean test`: 39 testes padrão, incluindo os 10 cenários do enunciado.
- `mvn compile exec:java "-Dexec.args=demo"`: reserva, empréstimo, renovação, devolução atrasada e multa de R$ 5,00, sempre em memória isolada.
- `mvn compile exec:java "-Dexec.args=menu memoria B"`: menu com o banco B simulado.
- Opção 2: manutenção separada dos cadastros de Pessoa, Estudante, Atendente e Livro (inserir, consultar, atualizar e excluir). Estudante e Atendente só são cadastrados se a Pessoa já existir, como no diagrama de cadastros.
- Opção 8: consulta de livros com metadados e disponibilidade atual. Por padrão, cache e medição estão habilitados; propriedades `consulta.cache` e `consulta.medicao` permitem desativá-los.
- Opções 10 a 13: criação e consulta da hierarquia do acervo, inserção e movimentação dos livros. A hierarquia é mantida durante a sessão.

A multa é vinculada ao identificador da devolução e não é emitida em duplicidade. A quitação é registrada pelo tratador a partir da notificação traduzida pelo adaptador.

## Limites e preparação para a entrega

- O schema novo vincula cada boleto a uma devolução. Use uma base de validação nova ou planeje a migração dos registros existentes; não apague históricos.
- Os bancos permanecem simulados. A deduplicação de notificações é local à instância; reinícios e conciliação financeira real não foram homologados.
- Os modelos foram regenerados no Astah UML em 22/11. Para reproduzir a exportação ou atualizá-la após alterações, consulte [instruções de atualização dos modelos](docs/uml/README.md).
- O histórico da auditoria anterior foi preservado em [docs/AUDITORIA_21-11.md](docs/AUDITORIA_21-11.md).

Para a apresentação, execute os testes, mostre a demo e prepare uma extensão simples de Strategy ou Observer. Cada integrante precisa saber explicar os dez padrões.
