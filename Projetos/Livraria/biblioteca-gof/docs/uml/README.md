# Modelos UML — procedência e revisão

## Arquivos da entrega

- `originais/Sistema de Empréstimo de Livros.asta`: cópia integral do modelo fornecido pela equipe, sem edição. SHA-256: `fafe0cc6daa01fb16a65cae8f8e3239e362e58186292c5c8521109854e038625`.
- `Biblioteca-GoF.asta`: revisão de Sérgio Calazans com apoio do Codex, construída pela API oficial do Astah UML 12.0 e validada/reaberta pela própria ferramenta. Os pacotes `Historico T02 - ...` mantêm a modelagem anterior; o pacote `Implementacao GoF 2026 - revisao Sergio Calazans` representa o código atual.
- `Diagramas-GoF.pdf`: 21 páginas, uma por diagrama atual, geradas a partir da exportação PNG oficial do Astah. Não se apresenta esta revisão como modelo original da equipe.
- `ferramentas/AtualizarModelos.java` e `ferramentas/exportar_pdf.py`: fontes reproduzíveis da atualização e da composição do PDF.

O original fornecido não continha os dez diagramas de classes GoF. A revisão acrescenta dez diagramas de classes (R01–R10), dez sequências dos cenários e uma máquina de estados de Livro. A edição foi necessária para entregar os modelos exigidos e refletir as correções verificadas, sem substituir ou sobrescrever o original.

## Escopo e limites

As classes, heranças, realizações e associações de campos são extraídas por reflexão das classes compiladas. Cada visão apresenta um subconjunto de até três operações declaradas, com tipos reais; `arg1`, `arg2` etc. representam parâmetros cujos nomes não foram conservados na compilação. Tipos genéricos de retorno estão resumidos. Classes compartilhadas aparecem em diferentes visões para facilitar a leitura, com o nome Java completo na definição. Não se trata de engenharia reversa completa de todas as classes.

As sequências são sínteses dos métodos e testes, com condições descritas nos rótulos. Não representam todas as ativações, retornos, alternativas ou entrelaçamentos possíveis. R05 mostra o cenário de regressão com e-mail primeiro para demonstrar isolamento de falha; o `main` registra inspeção, multa, indicadores e e-mail. R06 resume o efeito de duas submissões protegidas pelo monitor, sem pretender representar todos os escalonamentos de threads. As condições de erro indicam interrupção/rollback, não execução conjunta dos caminhos alternativos.

O PDF utiliza páginas dimensionadas ao diagrama para preservar a leitura por zoom. A estrutura do arquivo Astah foi validada pela API; isso não substitui a revisão semântica da equipe e a explicação individual ao professor.

## Reconciliação com o Trabalho 02

| Aspecto histórico | Implementação e revisão atual | Justificativa |
|---|---|---|
| Guarda `numRenovacoes <= 2` | `numRenovacoes < 2` | Enunciado GoF exige recusar terceira renovação antes de mudar a data |
| Tratadores separados de Pessoa/Estudante/Atendente/Acervo | `TratadorDeCadastro` implementa as interfaces de cadastro e usa templates | Preservar arquitetura Java existente; não criar classes vazias para coincidir com o desenho |
| Acervo | `RepositorioLivro`, Livro e família selecionada | Acesso ao acervo mantido atrás de interface |
| Relacionamento | `RepositorioRelacionamento` | Pessoas e papéis cadastrados antes de movimentações |
| Movimentacao/Transacao | `RepositorioMovimentacao`, Reserva, Emprestimo, Devolucao e BoletoMulta | Código não possui a antiga superclasse Transacao; revisão não a inventa |
| Livro com ISBN/autor | Livro com identificador inteiro e título | Modelo atual segue os atributos realmente implementados |
| Sequências anteriores | Fachada transacional, State e publicação após commit | Refletir rollback, regras atuais e independência dos ouvintes |
| Exclusão de Livro | Flag `excluido`, sem oitavo estado | Reserva é bloqueada; sete estados GoF preservados |

## Reproduzir a revisão

Na pasta `biblioteca-gof`, com JDK 17+, Maven, Astah UML 12 instalado/licenciado e Python com Pillow e reportlab:

```powershell
mvn compile
$astahDir = "C:/Program Files/astah-uml"
java "-Djava.awt.headless=true" -cp "$astahDir/astah-api.jar;$astahDir/astah-uml.jar;target/classes" docs/uml/ferramentas/AtualizarModelos.java "docs/uml/originais/Sistema de Empréstimo de Livros.asta" docs/uml/Biblioteca-GoF.asta
& "$astahDir/jre/bin/java.exe" "-Djava.awt.headless=true" "-Djava.library.path=$astahDir/lib/rlm" -cp "$astahDir/astah-uml.jar" com.change_vision.jude.cmdline.JudeCommandRunner -image all -f "$PWD/docs/uml/Biblioteca-GoF.asta" -t png -o "$PWD/target/uml-export"
python docs/uml/ferramentas/exportar_pdf.py target/uml-export docs/uml/Diagramas-GoF.pdf
```

Os comandos foram executados com os caminhos locais dos runtimes disponíveis. O gerador recusa entrada e saída com o mesmo caminho; sempre utilize o original como entrada. Backups do Astah e imagens temporárias não são versionados. Para alterações manuais, abra somente a revisão e exporte novamente. Se modificar o gerador, confira a estrutura, os rótulos e o PDF antes de publicar.
