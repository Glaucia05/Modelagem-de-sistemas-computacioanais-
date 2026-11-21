package br.pucpr.bcc.biblioteca.ui;

import br.pucpr.bcc.biblioteca.interfaces.*;
import br.pucpr.bcc.biblioteca.r02_strategy.*;
import br.pucpr.bcc.biblioteca.r03_adapter.*;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r05_observer.*;
import br.pucpr.bcc.biblioteca.r06_command.ExecutorComando;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
import br.pucpr.bcc.biblioteca.tratadores.*;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApp {
    public static void main(String[] args) {
        String perfil = args.length > 1 ? args[1] : "memoria";
        try (PersistenciaFactory factory = ProvedorPersistencia.criarPorPerfil(perfil)) {
            ConfiguracaoMulta configMulta = new ConfiguracaoMulta(new MultaOriginalStrategy());
            String banco = args.length > 2 ? args[2] : "A";
            CobrancaInstitucional cobranca = switch (banco.toUpperCase(java.util.Locale.ROOT)) {
                case "A" -> new BancoAAdapter(new BancoASimulado());
                case "B" -> new BancoBAdapter(new BancoBSimulado());
                default -> throw new IllegalArgumentException("Banco deve ser A ou B");
            };

            OuvinteInspecao ouvinteInspecao = new OuvinteInspecao();
            OuvinteMulta ouvinteMulta = new OuvinteMulta();
            OuvinteIndicadores ouvinteIndicadores = new OuvinteIndicadores();
            OuvinteEmail ouvinteEmail = new OuvinteEmail();

            PublicadorDevolucao publicador = new PublicadorDevolucao();
            publicador.inscrever(ouvinteInspecao);
            publicador.inscrever(ouvinteMulta);
            publicador.inscrever(ouvinteIndicadores);
            publicador.inscrever(ouvinteEmail);

            CirculacaoFacade facade = new CirculacaoFacade(
            factory.unidadeTrabalho(), factory.repositorioLivro(),
            factory.repositorioMovimentacao(), factory.repositorioRelacionamento(), publicador
            );

            TratadorDeCadastro tratadorCadastro = new TratadorDeCadastro(factory.repositorioLivro(), factory.repositorioRelacionamento());
            iPessoa cadPessoa = tratadorCadastro;
            iEstudante cadEstudante = tratadorCadastro;
            iLivro cadLivro = tratadorCadastro;
            iEmprestimo tratadorEmprestimo = new TratadorDeEmprestimo(facade, new ExecutorComando());
            TratadorDeDevolucao tratadorDevolucao = new TratadorDeDevolucao(
            facade, factory.repositorioMovimentacao(), factory.repositorioRelacionamento(), configMulta, cobranca
            );

            if (args.length > 0 && "demo".equalsIgnoreCase(args[0])) {
                executarFluxoCompletoDemonstracao(cadPessoa, cadEstudante, cadLivro, tratadorEmprestimo, tratadorDevolucao);
                return;
            }

            try (Scanner sc = new Scanner(System.in)) {
                boolean executando = true;
                while (executando) {
                    System.out.println("\n=== Sistema de Gestão de Biblioteca (GoF) ===");
                    System.out.println("1 - Executar fluxo completo de demonstração");
                    System.out.println("2 - Cadastrar Pessoa, Estudante e Livro");
                    System.out.println("3 - Reservar Livro");
                    System.out.println("4 - Efetivar Empréstimo");
                    System.out.println("5 - Renovar Empréstimo");
                    System.out.println("6 - Registrar Devolução e Emitir Boleto de Multa");
                    System.out.println("7 - Inspecionar Livro / Laudo Irrecuperável");
                    System.out.println("8 - Consultar Livro");
                    System.out.println("9 - Configurar política de multa");
                    System.out.println("0 - Sair");
                    System.out.print("Escolha: ");
                    if (!sc.hasNextLine()) break;
                    String op = sc.nextLine().trim();

                    try {
                        switch (op) {
                            case "1" -> executarFluxoCompletoDemonstracao(cadPessoa, cadEstudante, cadLivro, tratadorEmprestimo, tratadorDevolucao);
                            case "2" -> {
                                System.out.print("CPF: "); String cpf = sc.nextLine().trim();
                                System.out.print("Nome: "); String nome = sc.nextLine().trim();
                                System.out.print("Sobrenome: "); String sob = sc.nextLine().trim();
                                cadPessoa.inserirPessoa(nome, sob, cpf);
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("Matrícula: "); String mat = sc.nextLine().trim();
                                cadEstudante.inserirEstudante(cpf, idEst, mat);
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("Título: "); String tit = sc.nextLine().trim();
                                System.out.println("Cadastrado: " + cadLivro.inserirLivro(idLiv, tit));
                            }
                            case "3" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(tratadorEmprestimo.solicitarReserva(UUID.randomUUID().toString(), "cli", idLiv, idEst, LocalDate.now()));
                            }
                            case "4" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(tratadorEmprestimo.efetivarEmprestimo(idLiv, idEst, LocalDate.now(), 7));
                            }
                            case "5" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(tratadorEmprestimo.renovarEmprestimo(UUID.randomUUID().toString(), "cli", idLiv, idEst));
                            }
                            case "6" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("Dias após hoje para simular devolução (ex: 12): "); int dias = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(tratadorDevolucao.registrarDevolucao(idLiv, idEst, LocalDate.now().plusDays(dias)));
                                System.out.println("Multa: " + tratadorDevolucao.emitirBoletoMulta(idLiv, idEst).orElse(null));
                            }
                            case "7" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("1 - Bom estado | 2 - Danificado | 3 - Laudo Irrecuperável: ");
                                String tipo = sc.nextLine().trim();
                                if ("1".equals(tipo)) System.out.println(tratadorDevolucao.inspecionarLivro(idLiv, true));
                                else if ("2".equals(tipo)) System.out.println(tratadorDevolucao.inspecionarLivro(idLiv, false));
                                else if ("3".equals(tipo)) System.out.println(tratadorDevolucao.emitirLaudoIrrecuperavel(idLiv));
                            }
                            case "8" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(cadLivro.consultarLivro(idLiv).orElse(null));
                            }
                            case "9" -> {
                                System.out.print("Política (original, teto, tolerancia): ");
                                tratadorDevolucao.definirPoliticaMulta(sc.nextLine().trim());
                                System.out.println("Política atualizada para novas emissões.");
                            }
                            case "0" -> executando = false;
                            default -> System.out.println("Opção inválida.");
                        }
                    } catch (Exception ex) {
                        System.out.println("Erro na operação: " + ex.getMessage());
                    }
                }
            }
        }

    }

    public static void executarFluxoCompletoDemonstracao(
    iPessoa cadPessoa,
    iEstudante cadEstudante,
    iLivro cadLivro,
    iEmprestimo emprestimo,
    iDevolucao devolucao
    ) {
        cadPessoa.inserirPessoa("Glaucia", "Araujo", "12345678900");
        cadEstudante.inserirEstudante("12345678900", 10, "2026101");
        cadLivro.inserirLivro(1, "Design Patterns");

        LocalDate hoje = LocalDate.of(2026, 11, 20);
        System.out.println("1. Reserva: " + emprestimo.solicitarReserva("CMD-1", "estudante-10", 1, 10, hoje));
        System.out.println("2. Empréstimo: " + emprestimo.efetivarEmprestimo(1, 10, hoje, 7));
        System.out.println("3. Renovação: " + emprestimo.renovarEmprestimo("CMD-2", "estudante-10", 1, 10));
        System.out.println("4. Devolução atrasada: " + devolucao.registrarDevolucao(1, 10, hoje.plusDays(42)));
        System.out.println("5. Boleto multa: " + devolucao.emitirBoletoMulta(1, 10).orElse(null));
    }
}
