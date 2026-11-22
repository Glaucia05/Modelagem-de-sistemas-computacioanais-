# Modelos UML — procedência e revisão

## Arquivos

- `originais/Sistema de Empréstimo de Livros.asta`: cópia integral do modelo da equipe (Trabalho 02), sem edição.
- `Biblioteca-GoF.asta`: revisão gerada a partir do original, com a API do Astah UML. Os pacotes `Historico T02 - ...` mantêm a modelagem anterior; o pacote de implementação GoF representa o código atual.
- `Diagramas-GoF.pdf`: um diagrama por página, exportado do Astah a partir da revisão. Não é o modelo original da equipe.
- `ferramentas/AtualizarModelos.java` e `ferramentas/exportar_pdf.py`: geram a revisão e o PDF.

O original não continha os dez diagramas de classes dos padrões GoF. A revisão acrescenta dez diagramas de classes (R01–R10), dez diagramas de sequência dos cenários e a máquina de estados de `Livro`, sem sobrescrever o original. O uso de ferramentas de IA está descrito em `REGISTRO_LLM.md`.

## Escopo e limites

- Classes, heranças, realizações e associações são extraídas por reflexão das classes compiladas. Cada visão mostra até três operações por classe; `arg1`, `arg2` etc. indicam parâmetros cujos nomes não são conservados na compilação.
- Os diagramas de sequência são sínteses dos métodos e dos testes. Não representam todas as ativações, retornos ou alternativas possíveis.
- Não é engenharia reversa completa. A equipe deve revisar os diagramas antes da arguição.

## Reconciliação com o Trabalho 02

| Aspecto do Trabalho 02 | Implementação atual | Justificativa |
|---|---|---|
| Guarda `numRenovacoes <= 2` | `numRenovacoes < 2` | O enunciado GoF exige recusar a terceira renovação antes de alterar o vencimento |
| Tratadores separados de Pessoa, Estudante, Atendente e Acervo | `TratadorDeCadastro` implementa as interfaces de cadastro e usa os templates | Uma única entrada de cadastro, com o Template Method (R07) |
| `AcervoDeLivros` | Interface `AcervoDeLivros`, com `MemoriaAcervoDeLivros` e `MysqlAcervoDeLivros` | Nome do Diagrama de Classes; ponto interno "Acervo" |
| `GrupoDePessoas`, `GrupoDeAcademicos`, `GrupoDeColaboradores` | Interfaces de mesmo nome, reunidas por `Relacionamento` | Nomes do modelo; ponto interno "Relacionamento" |
| `Movimentacao` | Interface `Movimentacao` com `consultarReserva`, `consultarEmprestimo`, `consultarDevolucao`, `inserirReserva`, `inserirEmprestimo`, `inserirDevolucao` e `removerReserva` | Operações com os nomes do Diagrama de Classes |
| `Transacao` | Classe abstrata `Transacao`; `Reserva`, `Emprestimo` e `Devolucao` herdam dela | Diagrama de Domínio; `Emprestimo` tem `consultarEstado()` e `renovar(dias)` |
| Operações dos tratadores | `solicitarReservaLivro`, `consultarReservaLivro`, `emprestarLivro`, `renovarEmprestimoLivro`, `registrarDevolucaoLivro`, `emitirBoletoMulta`, `calcularMulta` | Nomes do Diagrama de Classes; `iEmprestimo` e `iDevolucao` mantêm os nomes do Diagrama de Componentes. Os parâmetros extras (`idComando`, `autor`, `data`) vêm do Command (R06); a multa é `BigDecimal`, não `double` (R02) |
| Livro com ISBN e autor | Livro com identificador inteiro e título | O modelo segue os atributos implementados |
| Sequências anteriores | Fachada transacional, State e publicação após o commit | Refletem rollback, regras atuais e independência dos ouvintes |
| Exclusão de Livro | Flag `excluido`, sem oitavo estado | Bloqueia a reserva e preserva os sete estados |

## Regenerar os modelos

Na pasta `biblioteca-gof`, com JDK 17+, Maven, Astah UML instalado e licenciado, e Python com Pillow e reportlab (ajuste `$astahDir` ao seu caminho):

```powershell
mvn compile
$astahDir = "C:/Program Files/astah-uml"
java "-Djava.awt.headless=true" -cp "$astahDir/astah-api.jar;$astahDir/astah-uml.jar;target/classes" docs/uml/ferramentas/AtualizarModelos.java "docs/uml/originais/Sistema de Empréstimo de Livros.asta" docs/uml/Biblioteca-GoF.asta
& "$astahDir/jre/bin/java.exe" "-Djava.awt.headless=true" "-Djava.library.path=$astahDir/lib/rlm" -cp "$astahDir/astah-uml.jar" com.change_vision.jude.cmdline.JudeCommandRunner -image all -f "$PWD/docs/uml/Biblioteca-GoF.asta" -t png -o "$PWD/target/uml-export"
python docs/uml/ferramentas/exportar_pdf.py target/uml-export docs/uml/Diagramas-GoF.pdf
```

O gerador recusa entrada e saída com o mesmo caminho: use sempre o original como entrada. Para alterações manuais, abra somente a revisão e exporte de novo.
