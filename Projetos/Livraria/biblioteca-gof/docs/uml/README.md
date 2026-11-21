# Modelos UML — procedência

Os modelos válidos deste projeto são os **elaborados pela equipe** (Glaucia Bispo, Nicolly Stefany e Sérgio Calazans). Nenhum diagrama é gerado automaticamente a partir do código.

## Arquivos

- `originais/Sistema de Empréstimo de Livros.asta`: modelo Astah da equipe, preservado sem edição. SHA-256: `fafe0cc6daa01fb16a65cae8f8e3239e362e58186292c5c8521109854e038625`.
- `../modelagem/Trabalho_1_Modelagem.pdf`: casos de uso, domínio, sequências (Manter Pessoa/Estudante/Atendente, Emprestar, Renovar, Registrar Devolução, Emitir Boleto de Multa) e classes de projeto.
- `../modelagem/Diagramas_UML_Estados_em_Diante.pdf`: estados de Livro, atividades (cadastros, devolução/inspeção/multa/pagamento, empréstimo, renovação, reserva), componentes, implantação e instanciação.

## Pendências de modelagem (a cargo da equipe)

Os diagramas de classes e de sequência dos dez padrões GoF ainda devem ser desenhados no Astah, a partir das modelagens acima, usando os nomes de `../CORRESPONDENCIA_NOMES.md`. Divergências conhecidas entre modelo e código:

| Item | Modelagem | Código | Ação sugerida |
|---|---|---|---|
| Guarda de renovação | `numRenovacoes<=2` (estados) | `numRenovacoes < 2` (premissa do enunciado) | Ajustar o diagrama de estados |
| Superclasse `Transacao` | Reserva, Emprestimo e Devolucao herdam de Transacao | Classes independentes | Criar a superclasse ou remover do modelo |
| Métodos do tratador de empréstimo | Classe de projeto: `solicitarReservaLivro`, `emprestarLivro`, `renovarEmprestimoLivro`; componente `iEmprestimo`: `solicitarReserva`, `efetivarEmprestimo`, `renovarEmprestimo` | Segue `iEmprestimo` | Unificar os dois diagramas |
| Exclusão de livro | Estados LIVRE e INCINERADO → final | Flag `excluido`, sem oitavo estado | Documentar no modelo |
