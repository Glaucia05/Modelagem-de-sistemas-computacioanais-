# Registro de Uso de LLMs

Este registro descreve o uso de duas ferramentas de IA no projeto: o Gemini (Google), na implementação inicial, e o Codex (OpenAI), na auditoria e na finalização de 21/11/2026. As duas também ajudaram na documentação: o Gemini apoiou a documentação técnica inicial, e o Codex redigiu o `TUTORIAL_EXECUCAO.md`, atualizou o README e este registro e a partir de decisões técnicas de documentação da equipe escreveu a documentação de procedência dos modelos UML. Os registros de cada etapa abaixo foram mantidos como foram escritos na época.

## Ferramentas e Modelos Utilizados
- **Modelo:** Gemini (Google)
- **Versão do modelo:** não registrada.
- **Finalidade:** Apoio na estruturação do projeto Maven em Java 17, geração das classes dos 10 padrões GoF, implementação das interfaces do diagrama de componentes (`iLivro`, `iPessoa`, `iEstudante`, `iAtendente`, `iEmprestimo`, `iDevolucao`), persistência JDBC MySQL com `schema.sql`, suíte de testes JUnit 5 e documentação técnica.

## Principais Pedidos e Decisões da Equipe
1. **Arquitetura e coerência com os diagramas UML:**
   - *Pedido:* Gerar o código Java 17 alinhado aos diagramas UML da equipe (máquina de estados de `Livro`, diagrama de componentes, tratadores e fluxos de atividade/sequência).
   - *Aceito:* Preservação das interfaces `iLivro`, `iPessoa`, `iEstudante`, `iAtendente`, `iEmprestimo` e `iDevolucao`, delegação dos tratadores para `CirculacaoFacade` e `RotinaCadastroTemplate`, e isolamento total de regras de domínio na interface `ConsoleApp`.
   - *Ajustado/Verificado:* Implementada guarda `numRenovacoes < 2` nos estados `EMPRESTADO` e `RENOVADO`, transição `REPARANDO -> INCINERADO` por laudo irrecuperável, proteção contra dupla quitação nos adaptadores bancários e persistência JDBC transacional na `MysqlPersistenciaFactory`.
2. **Suíte de testes automatizados (`RequisitosGoFTest`):**
   - *Pedido:* Criar testes automatizados JUnit 5 cobrindo os 10 cenários de verificação (`R01` a `R10`).
   - *Aceito e validado:* Execução automatizada em um único comando (`mvn clean test`).

- **Descartado nesta etapa:** nenhuma sugestão do Gemini foi registrada como descartada.


## Auditoria e correções com Codex 

- **Ferramenta/modelo identificável:** Codex, OpenAI; família GPT-6 indicada pelo ambiente. Variante exata não exposta. Nenhum agente auxiliar foi usado.
- **Solicitação:** auditoria técnica completa em Java/Maven/JUnit, leitura do enunciado, inspeção do Astah, verificação dos dez padrões, correção de defeitos comprovados, regressões, demonstração e relatório A–F. A solicitação autorizou alterações locais e vedou commits/pushes/merges automáticos.
- **Fontes consultadas:** PDF de cinco páginas fornecido pelo usuário, todos os fontes Java e documentos do projeto, schema SQL e modelo Astah original lido pela API oficial instalada. Documentos foram usados como evidência de requisitos, não como autorização para outras ações.
- **Diagnóstico original executado:** 10 testes, 8 aprovados, falha R09 e erro R10. As alegações anteriores de validação são preservadas como registro histórico; esta auditoria identificou falhas reais na versão recebida.
- **Corrigido (falhas do código da etapa anterior, aplicadas pelo Codex sob a autorização do pedido e sujeitas à revisão da equipe):** rollback em memória com cópias e snapshots; identidade/UPDATE de empréstimos JDBC; reconstituição completa; tratamento de conexão/commit/rollback e fechamento; validação de família; proteção contra órfãos; Command atômico; Composite com pai/ciclos/transferência; composição de R09; proteção do livro excluído e de eventos/datas inválidos; conversão de CPF B e validação/deduplicação bancária; snapshot da política e menu administrativo; data correta da demo; validação de Pessoa e diagnóstico de ouvintes.
- **Testes:** mantidas as asserções dos cenários existentes, com correção da composição inválida de R09 e asserção exata das duas medições. Criadas `AuditoriaRegressaoTest` e `PersistenciaJdbcTest`. H2 adicionado exclusivamente ao escopo de testes para executar SQL, sem exigir MySQL dos testes em memória. Resultado alcançado: 32 aprovados, sem falhas, erros ou ignorados; demonstração em memória emite R$ 5,00 após renovação.
- **Justificativas:** cada alteração responde a inconsistência observada no código, teste reprovado ou lacuna demonstrável de integridade. A identidade persistida do empréstimo evita múltiplas linhas ativas para a mesma operação. O monitor compartilhado torna a operação local indivisível; snapshots permitem desfazer a família em memória. A medição externa vê também cache hits, mantendo cache restrito aos metadados.
- **Descartado (sugestões não implementadas):** reescrever o projeto, substituir tratadores, adicionar frameworks de aplicação, afrouxar expectativas para ocultar falhas, exigir MySQL para testes unitários, inventar diagramas GoF como se fossem os originais, alterar a regra de duas renovações para acompanhar a guarda incorreta do Astah, executar migração/desduplicação destrutiva ou realizar operações Git de publicação.
- **Preservação:** registros anteriores mantidos; pacotes e nomes públicos existentes preservados; interfaces só receberam extensões compatíveis para identidade da unidade/fechamento. Nenhum banco externo foi alterado e nenhum diagrama foi salvo/modificado. O relatório completo, matriz, evidências e pendências estão no README.


### Revisão, correção e finalização 

**Ferramenta:** OpenAI Codex

**Modelo informado pelo usuário:** GPT-6 Astra Médio

**Finalidade:** auditoria técnica, revisão dos padrões GoF, correções, testes, validação arquitetural, documentação e preparação para entrega.

Etapa de 21/11/2026. O nome de modelo acima registra a informação do usuário; não constitui verificação independente da variante em execução. Os registros anteriores do Gemini e da primeira auditoria foram preservados integralmente e descrevem suas respectivas etapas.

- **Principais prompts:** continuar a partir dos 32 testes aprovados; homologar R08 com MySQL real se disponível; reconciliar o Astah sem apresentar modelos novos como originais; criar tutorial reproduzível; preservar o histórico Gemini; separar commits em português; criar/publicar `sergio-calazans/finalizacao-gof` e preparar PR sem merge. O usuário autorizou expressamente commits e push nesta etapa, revogando a restrição da auditoria anterior.
- **Fontes e arquivos analisados:** enunciado PDF de cinco páginas; original Astah; fontes Java de domínio, dez padrões, interfaces, tratadores e CLI; `pom.xml`, `schema.sql`, README, registro de IA e todas as classes de teste. O código existente e os testes orientaram os novos diagramas.
- **Aceito e implementado (sob autorização):** preservadas as correções anteriores; IDs dos bancos simulados passaram de contadores por instância a UUID com prefixos BA/BB, evitando colisões com boletos persistidos após reinício; nova regressão de IDs e verificação de recuperação do boleto no MySQL. Nenhuma implementação inicial do Gemini foi atribuída ao Codex.
- **Integração:** criado perfil Maven opcional `mysql-it` e `MysqlIntegracaoIT`, restrito à base `biblioteca_gof_validacao`. Verifica conexão/UOW compartilhada, fluxo, limite de renovação, boleto, uma linha de empréstimo, rollback, estados, FKs e visibilidade em outra conexão. Credenciais foram disponibilizadas pelo usuário em variáveis locais; não foram impressas nem gravadas no repositório. O teste conserva registros e só cria tabelas ausentes.
- **Testes e execução:** 33 testes padrão aprovados (os 32 anteriores mais a regressão de IDs), com zero falhas, erros ou ignorados; 1 teste adicional aprovado em MySQL 8.0.43. Compilação com `--release 17` em JDK 25.0.3, demo automática com multa R$ 5,00 e menu por entrada de terminal. Java 17 exato, VS Code, SMTP/bancos financeiros reais e Oracle não foram homologados.
- **Modelagem:** cópia original Astah preservada por hash; revisão separada com 10 classes, 10 sequências e estados de Livro, criada/validada/exportada pela API/CLI oficial Astah 12. O PDF deriva dessa exportação. Corrigida na revisão a guarda `<2`; nomes e operações refletem o código. As visões são resumidas, com limites explicitados no README UML. 
- **Documentação:** Codex redigiu `TUTORIAL_EXECUCAO.md`, atualizou rastreabilidade/resultados/limites no README e criou a documentação de procedência UML.
- **Descartado:** reescrita, frameworks adicionais, outbox, transações distribuídas, banco de chaves duráveis e webhook financeiro real. Não são necessários aos cenários acadêmicos exigidos e ampliariam o escopo sem benefício demonstrado. Também foram descartados limpeza de dados, alteração do original Astah, redução de expectativas dos testes e alegação de nota 100 garantida.
- **Limitações remanescentes:** idempotência do executor e deduplicação do adaptador locais à instância; emissão repetida não possui chave durável; falha entre banco simulado e gravação requer conciliação; cache sem TTL; carga/concorrência entre processos não homologadas.
- **Git:** Codex preparou a branch solicitada e os commits lógicos em português, inspecionando diffs e exclusão de credenciais/temporários. Publicação autorizada apenas nessa branch e PR para revisão, sem merge, force push, tags ou releases. O resultado efetivo da publicação e os hashes não constam neste repositório; foram apresentados apenas na resposta da ferramenta.


## Alinhamento dos nomes com as modelagens da equipe

- **Ferramenta:** Claude (Anthropic).
- **Pedido da equipe:** usar no projeto os nomes das modelagens da equipe (`docs/modelagem/`), não os do enunciado; registrar a correspondência nos documentos; descartar os arquivos `.asta` e PDF gerados automaticamente, pois os modelos válidos partem das modelagens da equipe.
- **Aceito e aplicado:** renomeação de pacotes (de `rNN_padrao` para pacotes por componente), `RepositorioLivro`→`AcervoDeLivros`, `RepositorioMovimentacao`→`Movimentacao`, `RepositorioRelacionamento`→`GruposDoSistema` (reunindo `GrupoDePessoas`, `GrupoDeAcademicos`, `GrupoDeColaboradores`), `PoliticaMulta`→`iMulta`, novas interfaces `iCobranca` e `iQuitacao`, e métodos de persistência para os nomes da classe de projeto. Tabelas em `docs/CORRESPONDENCIA_NOMES.md`.
- **Removido:** `docs/uml/Biblioteca-GoF.asta`, `docs/uml/Diagramas-GoF.pdf` e `docs/uml/ferramentas/`. Mantido o `.asta` original da equipe.
- **Verificação:** compilação do código principal e dos testes e execução dos 26 testes em memória (`RequisitosGoFTest` e `AuditoriaRegressaoTest`) com stubs mínimos de JUnit, sem Maven. `PersistenciaJdbcTest` (H2) e `MysqlIntegracaoIT` (MySQL) só foram compilados; a equipe deve rodar `mvn clean test` antes da entrega.
- **Não alterado:** nomes dos métodos de `iEmprestimo`, `iDevolucao` e demais interfaces de componente (já coincidem com o diagrama de componentes), nomes das classes dos padrões e testes `R01`…`R10`.
