# Uso de Ferramentas e Modelos (LLM) e Histórico de Mudanças

## Ferramentas e Modelos Utilizados

- **Gemini (Google):** Apoiou a estruturação inicial do projeto Java 17 com Maven, a implementação dos 10 padrões GoF, a criação das interfaces do diagrama de componentes (`iLivro`, `iPessoa`, `iEstudante`, `iAtendente`, `iEmprestimo`, `iDevolucao`), a definição inicial do `schema.sql` e dos repositórios JDBC do MySQL, além da primeira versão dos testes (`RequisitosGoFTest`), do `README.md` e do `REGISTRO_LLM.md`.
- **Codex (OpenAI — família GPT-6 indicada pelo ambiente):** Apoiou a auditoria técnica, a correção de defeitos de domínio, persistência e concorrência, a expansão da suíte de testes de regressão e integração, a reconciliação documental/UML e a revisão completa.

## Partes do Sistema e Mudanças Realizadas

- **Domínio e Máquina de Estados:**
  - Ajustes e validações no ciclo de vida de `Livro` e seus estados (`Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado`), incluindo bloqueio de reserva após exclusão lógica, validação de empréstimo vinculado, reconstituição completa dos estados/atributos e guarda de renovação (`< 2`).
  - Evolução do controle de empréstimos e devoluções (`Emprestimo`, `Devolucao`, `BoletoMulta`, `Reserva`) para evitar duplicidade de empréstimos ativos e preservar o estado persistido.
- **Políticas de Multa e Integração Bancária:**
  - Captura consistente da política vigente na emissão do boleto e seleção de política via tratador/menu (`MultaOriginalStrategy`, `MultaTetoVinteStrategy`, `MultaToleranciaDoisDiasStrategy`, `ConfiguracaoMulta`).
  - Tratamento de formatação de CPF, conferência de cobrança e valor, prevenção contra dupla quitação e geração de identificadores únicos (`UUID`) com prefixos `BA`/`BB` entre instâncias em `BancoASimulado`, `BancoBSimulado`, `BancoAAdapter` e `BancoBAdapter`.
- **Fachada , Observadores e Comandos:**
  - Reforço transacional em `CirculacaoFacade` (controle de início, commit/rollback, validação de unidade de trabalho e restauração de snapshots em memória).
  - Isolamento de falhas de ouvintes em `PublicadorDevolucao` e publicação de `EventoDevolucao` após o commit da devolução (`OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail`).
  - Execução e registro atômicos e idempotentes em `ExecutorComando` (`ReservarLivroCommand`, `RenovarEmprestimoCommand`).
- **Cadastros , Persistência  Consulta e Acervo :**
  - Preservação do fluxo final de `RotinaCadastroTemplate` e validações de pré-requisitos/campos obrigatórios (`CadastroPessoaTemplate`, `CadastroEstudanteTemplate`, `CadastroAtendenteTemplate`, `CadastroLivroTemplate`).
  - Ajustes na família em memória, na família relacional (`MysqlPersistenciaFactory` com transação compartilhada na `MysqlUnidadeTrabalho`, `FOR UPDATE` na leitura de livro e `INSERT ... ON DUPLICATE KEY UPDATE`) e em `schema.sql` (com restrições de integridade referencial para novas bases).
  - Ajuste na composição de `MedicaoConsultaDecorator`, `CacheMetadadosDecorator` e `ConsultaAcervoBase` para medir consultas e manter a consulta do estado atual separada na reserva.
  - Validações de hierarquia, pai único, ciclos, movimentação de itens e registro de folhas em `GrupoAcervoComposite` e `ItemLivroComposite`.
- **Interface , Testes e Documentação:**
  - Atualização do fluxo de demonstração (`demo`) e do menu interativo em `ConsoleApp`, expansão da suíte de testes JUnit 5 (incluindo `PersistenciaJdbcTest` e `MysqlIntegracaoIT`) e atualização de `README.md`e `REGISTRO_LLM.md`.
