import java.nio.file.*;
import java.lang.reflect.*;
import java.awt.geom.Point2D;
import java.util.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;

/** Revisão derivada do código compilado. Nunca salva sobre o original. */
public class AtualizarModelos {
    static ProjectAccessor acesso;
    static BasicModelEditor modelo;
    static IPackage atual;
    static final String PREFIXO="br.pucpr.bcc.biblioteca.";
    public static void main(String[] args) throws Exception {
        if(args.length!=2 || Path.of(args[0]).toAbsolutePath().equals(Path.of(args[1]).toAbsolutePath()))
            throw new IllegalArgumentException("Informe original.asta e revisao.asta distintos");
        acesso=AstahAPI.getAstahAPI().getProjectAccessor();
        acesso.open(args[0],true,false,true);
        try {
            TransactionManager.beginTransaction();
            modelo=ModelEditorFactory.getBasicModelEditor();
            for(INamedElement e:acesso.getProject().getOwnedElements())
                if(e instanceof IPackage) e.setName("Historico T02 - "+e.getName());
            atual=modelo.createPackage(acesso.getProject(),"Implementacao GoF");
            atual.setDefinition("Revisão identificada derivada do código Java e dos testes. Os pacotes Historico T02 preservam a modelagem anterior; consulte esta revisão para a implementação atual. Operações nos diagramas de classes são um subconjunto representativo da API real.");
        classes("R01-State", new String[]{"dominio.Livro","r01_state.EstadoLivro","r01_state.AbstractEstadoLivro","r01_state.LivreState","r01_state.ReservadoState","r01_state.EmprestadoState","r01_state.RenovadoState","r01_state.DevolvidoState","r01_state.ReparandoState","r01_state.IncineradoState"});
        classes("R02-Strategy", new String[]{"r02_strategy.ConfiguracaoMulta","r02_strategy.PoliticaMulta","tratadores.TratadorDeDevolucao","r02_strategy.MultaOriginalStrategy","r02_strategy.MultaTetoVinteStrategy","r02_strategy.MultaToleranciaDoisDiasStrategy","dominio.BoletoMulta"});
        classes("R03-Adapter", new String[]{"r03_adapter.CobrancaInstitucional","r03_adapter.BancoAAdapter","r03_adapter.BancoBAdapter","r03_adapter.NotificacaoPagamento","r03_adapter.BancoASimulado","r03_adapter.BancoBSimulado"});
        classes("R04-Facade", new String[]{"r04_facade.CirculacaoFacade","tratadores.TratadorDeEmprestimo","tratadores.TratadorDeDevolucao","r08_abstract_factory.AcervoDeLivros","r08_abstract_factory.Movimentacao","r08_abstract_factory.Relacionamento","r08_abstract_factory.UnidadeTrabalho","dominio.Livro","dominio.Transacao","dominio.Reserva","dominio.Emprestimo","dominio.Devolucao"});
        classes("R05-Observer", new String[]{"r05_observer.PublicadorDevolucao","r05_observer.OuvinteDevolucao","r05_observer.EventoDevolucao","r05_observer.OuvinteInspecao","r05_observer.OuvinteMulta","r05_observer.OuvinteIndicadores","r05_observer.OuvinteEmail"});
        classes("R06-Command", new String[]{"r06_command.ExecutorComando","r06_command.ComandoCirculacao","tratadores.TratadorDeEmprestimo","r06_command.ReservarLivroCommand","r06_command.RenovarEmprestimoCommand","r04_facade.CirculacaoFacade"});
        classes("R07-Template-Method", new String[]{"r07_template.RotinaCadastroTemplate","tratadores.TratadorDeCadastro","r07_template.CadastroPessoaTemplate","r07_template.CadastroEstudanteTemplate","r07_template.CadastroAtendenteTemplate","r07_template.CadastroLivroTemplate"});
        classes("R08-Abstract-Factory", new String[]{"r08_abstract_factory.PersistenciaFactory","r08_abstract_factory.MemoriaPersistenciaFactory","r08_abstract_factory.MysqlPersistenciaFactory","r08_abstract_factory.ProvedorPersistencia","r08_abstract_factory.UnidadeTrabalho","r08_abstract_factory.AcervoDeLivros","r08_abstract_factory.Movimentacao","r08_abstract_factory.Relacionamento","r08_abstract_factory.MemoriaPersistenciaFactory$MemoriaUnidadeTrabalho","r08_abstract_factory.MysqlPersistenciaFactory$MysqlUnidadeTrabalho","r08_abstract_factory.GrupoDePessoas","r08_abstract_factory.GrupoDeAcademicos","r08_abstract_factory.GrupoDeColaboradores","r08_abstract_factory.MemoriaPersistenciaFactory$MemoriaAcervoDeLivros","r08_abstract_factory.MemoriaPersistenciaFactory$MemoriaMovimentacao","r08_abstract_factory.MemoriaPersistenciaFactory$MemoriaRelacionamento","r08_abstract_factory.MysqlPersistenciaFactory$MysqlAcervoDeLivros","r08_abstract_factory.MysqlPersistenciaFactory$MysqlMovimentacao","r08_abstract_factory.MysqlPersistenciaFactory$MysqlRelacionamento"});
        classes("R09-Decorator", new String[]{"r09_decorator.ConsultaAcervoService","r09_decorator.ConsultaAcervoDecorator","r09_decorator.ConsultaAcervoBase","r09_decorator.MedicaoConsultaDecorator","r09_decorator.CacheMetadadosDecorator","r09_decorator.MetadadosLivro","tratadores.TratadorDeAcervo"});
        classes("R10-Composite", new String[]{"r10_composite.ElementoAcervo","r10_composite.GrupoAcervoComposite","r10_composite.ItemLivroComposite","tratadores.TratadorDeAcervo"});
        sequencia("R01-terceira-renovacao", new String[]{"Teste R01","CirculacaoFacade","AcervoDeLivros","Movimentacao","Livro","RenovadoState"}, new String[][]{{"0","1","renovarEmprestimo(1, 101)"},{"1","2","consultarLivro(1)"},{"1","3","consultarEmprestimo(1, 101)"},{"1","4","renovar(emprestimo)"},{"4","5","renovar(livro, emprestimo) [numRenovacoes = 2: false]"},{"0","1","solicitarReserva(...) [DEVOLVIDO: recusada]"}});
        sequencia("R02-politicas-multa", new String[]{"Teste R02","MultaOriginalStrategy","MultaToleranciaDoisDiasStrategy","MultaTetoVinteStrategy","ConfiguracaoMulta"}, new String[][]{{"0","1","calcular(vencimento, vencimento+5) : 5.00"},{"0","2","calcular(vencimento, vencimento+5) : 3.00"},{"0","3","calcular(vencimento, vencimento+30) : 20.00"},{"0","1","calcular(vencimento, vencimento) : 0.00"},{"0","4","definirPolitica(nova) [boletos emitidos preservados]"}});
        sequencia("R03-bancos", new String[]{"Teste R03","BancoAAdapter","BancoASimulado","BancoBAdapter","BancoBSimulado"}, new String[][]{{"0","1","registrarCobranca(ref, CPF, 7.00)"},{"1","2","criarBoletoCentavos(CPF numerico, 700)"},{"0","3","registrarCobranca(ref, CPF, 7.00)"},{"3","4","gerarCobrancaDecimal(CPF formatado, 7.00)"},{"0","1","processarNotificacao(LIQUIDADO) : QUITADO"},{"0","1","processarNotificacao(repetida) : vazio"}});
        sequencia("R04-emprestimo-atomico", new String[]{"TratadorDeEmprestimo","CirculacaoFacade","UnidadeTrabalho","AcervoDeLivros","Movimentacao","Livro"}, new String[][]{{"0","1","efetivarEmprestimo(...)"},{"1","2","iniciar()"},{"1","3","consultarLivro(idLivro)"},{"1","4","consultarReserva(idLivro, idEstudante)"},{"1","5","emprestar(idEstudante, emprestimo)"},{"1","4","removerReserva(idLivro, idEstudante)"},{"1","4","inserirEmprestimo(emprestimo)"},{"1","3","salvar(livro)"},{"1","2","commit() [falhas anteriores: rollback()]"}});
        sequencia("R05-devolucao-observada", new String[]{"CirculacaoFacade","UnidadeTrabalho","PublicadorDevolucao","OuvinteEmail","OuvinteInspecao","OuvinteMulta","OuvinteIndicadores"}, new String[][]{{"0","1","commit() [devolucao gravada]"},{"0","2","publicar(evento)"},{"2","3","aoDevolver(evento) [falha SMTP isolada]"},{"2","4","aoDevolver(evento) [fila de inspecao]"},{"2","5","aoDevolver(evento) [somente se atrasada]"},{"2","6","aoDevolver(evento) [indicadores atualizados]"}});
        sequencia("R06-comando-repetido", new String[]{"Submissao A","Submissao B","ExecutorComando","RenovarEmprestimoCommand","CirculacaoFacade"}, new String[][]{{"0","2","executar(comando ID-X) [monitor adquirido]"},{"2","3","executar()"},{"3","4","renovarEmprestimo(...) [+30 dias]"},{"1","2","executar(comando ID-X) [aguarda monitor; retorna resultado salvo]"}});
        sequencia("R07-cadastro", new String[]{"TratadorDeCadastro","CadastroEstudanteTemplate","Relacionamento"}, new String[][]{{"0","1","incluir() [metodo final herdado]"},{"1","1","buscarExistente()"},{"1","2","consultarEstudante(id)"},{"1","1","validar()"},{"1","1","verificarPreRequisitos()"},{"1","2","consultarPessoa(cpf) [ausente: interromper sem gravar]"},{"1","1","criar()"},{"1","1","armazenar(estudante)"},{"1","2","inserirEstudante(estudante)"},{"1","1","apresentar(estudante)"}});
        sequencia("R08-familia-jdbc", new String[]{"Aplicacao","ProvedorPersistencia","MysqlPersistenciaFactory","MysqlUnidadeTrabalho","MysqlAcervoDeLivros"}, new String[][]{{"0","1","criarPorPerfil(prod)"},{"0","2","unidadeTrabalho()"},{"0","2","acervoDeLivros() [mesma UOW]"},{"0","3","iniciar()"},{"0","4","salvar(livro)"},{"4","3","getConnection() [Connection compartilhada]"},{"0","3","commit()"},{"0","3","iniciar()"},{"0","4","salvar(outroLivro)"},{"0","3","rollback() [outroLivro ausente]"}});
        sequencia("R09-cache-medido", new String[]{"Teste R09","MedicaoConsultaDecorator","CacheMetadadosDecorator","ConsultaAcervoBase","AcervoDeLivros"}, new String[][]{{"0","1","consultarMetadados(1) [medicao 1]"},{"1","2","consultarMetadados(1) [miss]"},{"2","3","consultarMetadados(1)"},{"3","4","consultarLivro(1)"},{"0","1","consultarMetadados(1) [medicao 2]"},{"1","2","consultarMetadados(1) [hit; sem chamar Base]"},{"0","1","podeReservar(1)"},{"1","2","podeReservar(1)"},{"2","3","podeReservar(1)"},{"3","4","consultarLivro(1) [estado atual: EMPRESTADO, false]"}});
        sequencia("R10-hierarquia", new String[]{"Teste R10","unidade:GrupoAcervoComposite","secaoA:GrupoAcervoComposite","secaoB:GrupoAcervoComposite","folha:ItemLivroComposite"}, new String[][]{{"0","1","contarLivros()"},{"1","2","contarLivros() [3 folhas]"},{"2","4","contarLivros() : 1 [por folha]"},{"1","3","contarLivros() [2 folhas]"},{"0","2","moverItemPara(item, secaoB) [sem acessar Livro/Movimentacao]"},{"0","1","contarLivros() : 5"}});
            estados();
            TransactionManager.endTransaction();
            acesso.validateProject();
            acesso.saveAs(args[1]);
            System.out.println("Revisao salva e validada: "+args[1]);
        } finally { acesso.close(); }
        System.exit(0);
    }
    static void classes(String nome,String[] nomes) throws Exception {
        IPackage pacote=modelo.createPackage(atual,nome);
        var editor=acesso.getDiagramEditorFactory().getClassDiagramEditor();
        editor.createClassDiagram(pacote,nome+"-Classes");
        Map<Class<?>,IClass> classes=new LinkedHashMap<>();
        Map<Class<?>,INodePresentation> vistas=new LinkedHashMap<>();
        int i=0;
        for(String n:nomes) {
            Class<?> tipo=Class.forName(PREFIXO+n);
            IClass c=tipo.isInterface()?modelo.createInterface(pacote,tipo.getSimpleName()):modelo.createClass(pacote,tipo.getSimpleName());
            c.setDefinition("Classe real: "+tipo.getName()+". Código-fonte é a referência completa de assinaturas.");
            if(Modifier.isAbstract(tipo.getModifiers())&&!tipo.isInterface()) c.setAbstract(true);
            if(tipo.isRecord()) c.addStereotype("record");
            classes.put(tipo,c);
        }
        for(Class<?> tipo:classes.keySet()) {
            IClass c=classes.get(tipo);
            List<String> essenciais = operacoesEssenciais(nome, tipo);
            Arrays.stream(tipo.getDeclaredMethods()).filter(m -> !m.isSynthetic() && !Modifier.isStatic(m.getModifiers())
                && (Modifier.isPublic(m.getModifiers())||Modifier.isProtected(m.getModifiers()))
                && !Set.of("equals","hashCode","toString").contains(m.getName()))
                .sorted(Comparator.comparingInt((Method m) -> {
                    var prioridade = essenciais.isEmpty() ? List.of("incluir","executar","publicar","aoDevolver","calcular","registrarCobranca","processarNotificacao","solicitarReserva","efetivarEmprestimo","renovarEmprestimo","reservar","renovar","devolver","emprestar","inspecionar","incinerar","consultarMetadados","podeReservar","contarLivros","listarIdsLivros","adicionar","moverItemPara","unidadeTrabalho","acervoDeLivros","iniciar","commit","rollback","getPoliticaVigente","definirPolitica") : essenciais;
                    int pos=prioridade.indexOf(m.getName());return pos<0?999:pos;
                }).thenComparing(Method::getName)).limit(essenciais.isEmpty() ? 3 : essenciais.size()).forEach(m -> {
                    try {
                        IOperation op=modelo.createOperation(c,m.getName(),m.getReturnType().getSimpleName());
                        op.setVisibility(Modifier.isProtected(m.getModifiers())?"protected":"public");
                        int k=0; for(Class<?> t:m.getParameterTypes()) modelo.createParameter(op,"arg"+(++k),t.getSimpleName());
                    } catch(Exception ex) {throw new IllegalStateException(ex);}
                });
            INodePresentation vista=editor.createNodePresentation(c,posicao(nome,i));
            vista.setWidth(360); if(tipo.isInterface()) vista.setProperty("notation_type","normal"); vistas.put(tipo,vista);i++;
        }
        for(Class<?> tipo:classes.keySet()) {
            Class<?> pai=tipo.getSuperclass();
            if(classes.containsKey(pai)) editor.createLinkPresentation(modelo.createGeneralization(classes.get(tipo),classes.get(pai),""),vistas.get(tipo),vistas.get(pai));
            for(Class<?> iface:tipo.getInterfaces()) if(classes.containsKey(iface))
                editor.createLinkPresentation(modelo.createRealization(classes.get(tipo),classes.get(iface),""),vistas.get(tipo),vistas.get(iface));
            for(Field campo:tipo.getDeclaredFields()) {
                if(Modifier.isStatic(campo.getModifiers())||campo.isSynthetic())continue;
                Class<?> alvo=campo.getType(); boolean muitos=false;
                if(campo.getGenericType() instanceof ParameterizedType pt) {
                    for(Type t:pt.getActualTypeArguments()) if(t instanceof Class<?> c && classes.containsKey(c)) {alvo=c;muitos=true;}
                }
                if(classes.containsKey(alvo)) {
                    IAssociation assoc=modelo.createAssociation(classes.get(tipo),classes.get(alvo),"","",campo.getName());
                    if(muitos) assoc.getMemberEnds()[1].setMultiplicityString("0..*");
                    if(tipo.getSimpleName().equals("GrupoAcervoComposite") && campo.getName().equals("filhos")) assoc.getMemberEnds()[0].setComposite();
                    editor.createLinkPresentation(assoc,vistas.get(tipo),vistas.get(alvo));
                }
            }
        }
        System.out.println("Classes: "+nome+" ("+i+")");
    }
    static List<String> operacoesEssenciais(String diagrama, Class<?> tipo) {
        String classe = tipo.getSimpleName();
        if (diagrama.startsWith("R08") && classe.endsWith("PersistenciaFactory")) {
            return List.of("unidadeTrabalho", "acervoDeLivros", "movimentacao", "relacionamento", "familia");
        }
        if (diagrama.startsWith("R10")) {
            if (classe.equals("GrupoAcervoComposite")) return List.of("adicionar", "moverItemPara", "contarLivros", "listarIdsLivros", "contemGrupo");
            if (classe.equals("TratadorDeAcervo")) return List.of("criarGrupo", "adicionarLivro", "moverLivro", "listarGrupos", "contarLivros");
        }
        return List.of();
    }
    static Point2D posicao(String nome,int i) {
        double[][] p=switch(nome.substring(0,3)) {
            case "R01" -> new double[][]{{0,0},{1100,0},{1100,270},{0,650},{470,650},{940,650},{1410,650},{1880,650},{2350,650},{2820,650}};
            case "R02" -> new double[][]{{0,0},{650,0},{1300,0},{0,500},{650,500},{1300,500},{1950,0}};
            case "R03" -> new double[][]{{650,0},{0,450},{1300,450},{650,900},{0,900},{1300,900}};
            case "R04" -> new double[][]{{650,350},{0,0},{1300,0},{0,700},{650,700},{1300,700},{1950,350},{0,350},{650,1050},{0,1400},{650,1400},{1300,1400}};
            case "R05" -> new double[][]{{0,0},{900,0},{1800,0},{0,500},{600,500},{1200,500},{1800,500}};
            case "R06" -> new double[][]{{0,0},{800,0},{1600,0},{300,500},{1100,500},{1600,1000}};
            case "R07" -> new double[][]{{900,0},{1800,0},{0,500},{600,500},{1200,500},{1800,500}};
            case "R08" -> new double[][]{{1300,0},{0,400},{0,1600},{0,0},{1950,1000},{0,1000},{650,1000},{1300,1000},{1950,700},{1950,1300},{2600,750},{2600,1000},{2600,1250},{0,700},{650,700},{1300,700},{0,1300},{650,1300},{1300,1300}};
            case "R09" -> new double[][]{{650,0},{300,320},{1400,320},{0,650},{650,650},{1400,650},{0,0}};
            case "R10" -> new double[][]{{650,0},{300,400},{1400,400},{0,0}};
            default -> throw new IllegalArgumentException(nome);
        };
        if (i >= p.length) return new Point2D.Double(100 + (i-p.length)*650, Arrays.stream(p).mapToDouble(a -> a[1]).max().orElse(0)+650);
        return new Point2D.Double(p[i][0]+100,p[i][1]+100);
    }
    static void sequencia(String nome,String[] participantes,String[][] passos) throws Exception {
        var editor=acesso.getDiagramEditorFactory().getSequenceDiagramEditor();
        editor.createSequenceDiagram(atual,nome+"-Sequencia");
        List<INodePresentation> linhas=new ArrayList<>();
        for(int i=0;i<participantes.length;i++) linhas.add(editor.createLifeline(participantes[i],70+i*350));
        double y=160;
        for(String[] p:passos) {var mensagem=editor.createMessage("passo",linhas.get(Integer.parseInt(p[0])),linhas.get(Integer.parseInt(p[1])),y); ((INamedElement)mensagem.getModel()).setName(p[2].replace("[", " - ").replace("]", "").replace(":", " =").replace("(", " ").replace(")", ""));y+=100;}
        System.out.println("Sequencia: "+nome);
    }
    static void estados() throws Exception {
        var editor=acesso.getDiagramEditorFactory().getStateMachineDiagramEditor();
        editor.createStatemachineDiagram(atual,"R01-Livro-Estados");
        Map<String,INodePresentation> estados=new LinkedHashMap<>();
        String[] nomes={"LIVRE","RESERVADO","EMPRESTADO","RENOVADO","DEVOLVIDO","REPARANDO","INCINERADO"};
        double[][] pos={{100,100},{1000,100},{1900,100},{1900,700},{1000,700},{100,700},{100,1300}}; int i=0;for(String n:nomes) {var v=editor.createState(n,null,new Point2D.Double(pos[i][0],pos[i][1]));v.setWidth(250);estados.put(n,v);i++;}
        var inicio=editor.createInitialPseudostate(null,new Point2D.Double(20,120));
        editor.createTransition(inicio,estados.get("LIVRE"));
        trans(editor,estados,"LIVRE","RESERVADO","reservar","!excluido; estudante cadastrado","registrar destinatario");
        trans(editor,estados,"RESERVADO","EMPRESTADO","emprestar","reserva do mesmo estudante","numRenovacoes = 0");
        trans(editor,estados,"EMPRESTADO","RENOVADO","renovar","numRenovacoes < 2","vencimento +30 dias; renovacoes +1");
        trans(editor,estados,"RENOVADO","RENOVADO","renovar","numRenovacoes < 2","vencimento +30 dias; renovacoes +1");
        trans(editor,estados,"EMPRESTADO","DEVOLVIDO","devolver","emprestimo valido","");
        trans(editor,estados,"RENOVADO","DEVOLVIDO","devolver","emprestimo valido","");
        trans(editor,estados,"DEVOLVIDO","LIVRE","inspecionar","bomEstado","");
        trans(editor,estados,"DEVOLVIDO","REPARANDO","inspecionar","!bomEstado","");
        trans(editor,estados,"LIVRE","REPARANDO","enviarParaReparo","danoIdentificado","");
        trans(editor,estados,"REPARANDO","LIVRE","retomarDoReparo","","");
        trans(editor,estados,"REPARANDO","INCINERADO","incinerar","semReparo","");
        // Exclusão lógica não cria um oitavo EstadoLivro.
        trans(editor,estados,"LIVRE","LIVRE","excluir","","excluido = true; reserva bloqueada");
        trans(editor,estados,"INCINERADO","INCINERADO","excluir","","excluido = true");
    }
    static void trans(StateMachineDiagramEditor editor,Map<String,INodePresentation> e,String a,String b,String evento,String guarda,String acao) throws Exception {
        ILinkPresentation linha=editor.createTransition(e.get(a),e.get(b));
        if(a.equals("LIVRE") && b.equals("REPARANDO")) linha.setAllPoints(new Point2D[]{new Point2D.Double(100,125),new Point2D.Double(0,125),new Point2D.Double(0,725),new Point2D.Double(100,725)});
        if(a.equals("REPARANDO") && b.equals("LIVRE")) linha.setAllPoints(new Point2D[]{new Point2D.Double(350,725),new Point2D.Double(500,725),new Point2D.Double(500,125),new Point2D.Double(350,125)});
        ITransition t=(ITransition)linha.getModel();
        t.setEvent(evento); if(!guarda.isBlank()) t.setGuard(guarda); if(!acao.isBlank()) t.setAction(acao);
    }
}
