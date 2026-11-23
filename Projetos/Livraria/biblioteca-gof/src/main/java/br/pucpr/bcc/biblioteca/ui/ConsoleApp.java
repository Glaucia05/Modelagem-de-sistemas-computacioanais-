package br.pucpr.bcc.biblioteca.ui;

import br.pucpr.bcc.biblioteca.interfaces.*;
import br.pucpr.bcc.biblioteca.r02_strategy.*;
import br.pucpr.bcc.biblioteca.r03_adapter.*;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r05_observer.*;
import br.pucpr.bcc.biblioteca.r06_command.ExecutorComando;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
import br.pucpr.bcc.biblioteca.r09_decorator.*;
import br.pucpr.bcc.biblioteca.tratadores.*;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApp {
    public static void main(String[] args) {
        boolean demo = args.length > 0 && "demo".equalsIgnoreCase(args[0]);
        String perfil = !demo && args.length > 1 ? args[1] : "memoria";
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
            factory.unidadeTrabalho(), factory.acervoDeLivros(),
            factory.movimentacao(), factory.relacionamento(), publicador
            );

            TratadorDeCadastro tratadorCadastro = new TratadorDeCadastro(factory.acervoDeLivros(), factory.relacionamento());
            iPessoa cadPessoa = tratadorCadastro;
            iEstudante cadEstudante = tratadorCadastro;
            iLivro cadLivro = tratadorCadastro;
            ConsultaAcervoService consulta = new ConsultaAcervoBase(factory.acervoDeLivros());
            if (Boolean.parseBoolean(System.getProperty("consulta.cache", "true"))) consulta = new CacheMetadadosDecorator(consulta);
            if (Boolean.parseBoolean(System.getProperty("consulta.medicao", "true"))) consulta = new MedicaoConsultaDecorator(consulta);
            TratadorDeAcervo tratadorAcervo = new TratadorDeAcervo(factory.acervoDeLivros(), consulta);
            iEmprestimo tratadorEmprestimo = new TratadorDeEmprestimo(facade, new ExecutorComando());
            TratadorDeDevolucao tratadorDevolucao = new TratadorDeDevolucao(
            facade, factory.movimentacao(), factory.relacionamento(), configMulta, cobranca
            );

            if (demo) {
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
                    System.out.println("10 - Criar grupo do acervo");
                    System.out.println("11 - Adicionar livro ao grupo");
                    System.out.println("12 - Mover livro entre grupos");
                    System.out.println("13 - Listar grupos do acervo");
                    System.out.println("0 - Sair");
                    System.out.print("Escolha: ");
                    if (!sc.hasNextLine()) break;
                    String op = sc.nextLine().trim();

                    try {
                        switch (op) {
                            case "1" -> main(new String[]{"demo", "memoria", banco});
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
                                System.out.println(tratadorEmprestimo.solicitarReservaLivro(UUID.randomUUID().toString(), "cli", idLiv, idEst, LocalDate.now()));
                            }
                            case "4" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(tratadorEmprestimo.emprestarLivro(idLiv, idEst, LocalDate.now(), 7));
                            }
                            case "5" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.println(tratadorEmprestimo.renovarEmprestimoLivro(UUID.randomUUID().toString(), "cli", idLiv, idEst));
                            }
                            case "6" -> {
                                System.out.print("ID Livro: "); int idLiv = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("ID Estudante: "); int idEst = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("Dias após hoje para simular devolução (ex: 12): "); int dias = Integer.parseInt(sc.nextLine().trim());
                                var resultado = tratadorDevolucao.registrarDevolucaoLivro(idLiv, idEst, LocalDate.now().plusDays(dias));
                                System.out.println(resultado);
                                if (resultado.sucesso()) System.out.println("Multa: " + tratadorDevolucao.emitirBoletoMulta(idLiv, idEst).orElse(null));
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
                                System.out.println("Metadados: " + tratadorAcervo.consultarMetadados(idLiv).orElse(null));
                                System.out.println("Disponível para reserva: " + tratadorAcervo.podeReservar(idLiv));
                            }
                            case "9" -> {
                                System.out.print("Política (original, teto, tolerancia): ");
                                tratadorDevolucao.definirPoliticaMulta(sc.nextLine().trim());
                                System.out.println("Política atualizada para novas emissões.");
                            }
                            case "10" -> {
                                System.out.print("Grupo pai (ex: Unidade Central): "); String pai = sc.nextLine().trim();
                                System.out.print("Nome do grupo: "); String nome = sc.nextLine().trim();
                                tratadorAcervo.criarGrupo(pai, nome);
                            }
                            case "11" -> {
                                System.out.print("Grupo: "); String grupo = sc.nextLine().trim();
                                System.out.print("ID Livro: "); int id = Integer.parseInt(sc.nextLine().trim());
                                tratadorAcervo.adicionarLivro(grupo, id);
                            }
                            case "12" -> {
                                System.out.print("ID Livro: "); int id = Integer.parseInt(sc.nextLine().trim());
                                System.out.print("Origem: "); String origem = sc.nextLine().trim();
                                System.out.print("Destino: "); String destino = sc.nextLine().trim();
                                System.out.println("Movido: " + tratadorAcervo.moverLivro(id, origem, destino));
                            }
                            case "13" -> {
                                System.out.println(tratadorAcervo.listarGrupos());
                                System.out.println("Total de livros: " + tratadorAcervo.contarLivros());
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
        System.out.println("1. Reserva: " + emprestimo.solicitarReservaLivro("CMD-1", "estudante-10", 1, 10, hoje));
        System.out.println("2. Empréstimo: " + emprestimo.emprestarLivro(1, 10, hoje, 7));
        System.out.println("3. Renovação: " + emprestimo.renovarEmprestimoLivro("CMD-2", "estudante-10", 1, 10));
        System.out.println("4. Devolução atrasada: " + devolucao.registrarDevolucaoLivro(1, 10, hoje.plusDays(42)));
        System.out.println("5. Boleto multa: " + devolucao.emitirBoletoMulta(1, 10).orElse(null));
    }
}
