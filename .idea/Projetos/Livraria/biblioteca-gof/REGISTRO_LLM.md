# Registro de Uso de LLMs

## Ferramentas e Modelos Utilizados
- **Modelo:** Gemini (Google)
- **Finalidade:** Apoio na estruturação do projeto Maven em Java 17, geração das classes dos 10 padrões GoF, implementação das interfaces do diagrama de componentes (`iLivro`, `iPessoa`, `iEstudante`, `iAtendente`, `iEmprestimo`, `iDevolucao`), persistência JDBC MySQL com `schema.sql`, suíte de testes JUnit 5 e documentação técnica.

## Principais Pedidos e Decisões da Equipe
1. **Arquitetura e coerência com os diagramas UML:**
   - *Pedido:* Gerar o código Java 17 alinhado aos diagramas UML da equipe (máquina de estados de `Livro`, diagrama de componentes, tratadores e fluxos de atividade/sequência).
   - *Aceito:* Preservação das interfaces `iLivro`, `iPessoa`, `iEstudante`, `iAtendente`, `iEmprestimo` e `iDevolucao`, delegação dos tratadores para `CirculacaoFacade` e `RotinaCadastroTemplate`, e isolamento total de regras de domínio na interface `ConsoleApp`.
   - *Ajustado/Verificado:* Implementada guarda `numRenovacoes < 2` nos estados `EMPRESTADO` e `RENOVADO`, transição `REPARANDO -> INCINERADO` por laudo irrecuperável, proteção contra dupla quitação nos adaptadores bancários e persistência JDBC transacional na `MysqlPersistenciaFactory`.
2. **Suíte de testes automatizados (`RequisitosGoFTest`):**
   - *Pedido:* Criar testes automatizados JUnit 5 cobrindo os 10 cenários de verificação (`R01` a `R10`).
   - *Aceito e validado:* Execução automatizada em um único comando (`mvn clean test`).
