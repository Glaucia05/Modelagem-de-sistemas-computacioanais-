package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.r09_decorator.*;
import br.pucpr.bcc.biblioteca.r10_composite.*;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.AcervoDeLivros;
import java.util.*;

public class TratadorDeAcervo {
    private final AcervoDeLivros acervo;
    private final ConsultaAcervoService consulta;
    private final Set<Integer> ids = new HashSet<>();
    private final Map<String, GrupoAcervoComposite> grupos = new LinkedHashMap<>();
    private final GrupoAcervoComposite unidade = new GrupoAcervoComposite("Unidade Central", ids);

    public TratadorDeAcervo(AcervoDeLivros acervo, ConsultaAcervoService consulta) {
        this.acervo = acervo;
        this.consulta = consulta;
        grupos.put(unidade.nome(), unidade);
    }

    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return consulta.consultarMetadados(idLivro);
    }

    public boolean podeReservar(int idLivro) {
        return consulta.podeReservar(idLivro);
    }

    public void criarGrupo(String pai, String nome) {
        if (nome.isBlank() || grupos.containsKey(nome)) throw new IllegalArgumentException("Nome de grupo inválido ou duplicado");
        GrupoAcervoComposite novo = new GrupoAcervoComposite(nome, ids);
        obterGrupo(pai).adicionar(novo);
        grupos.put(nome, novo);
    }

    public void adicionarLivro(String grupo, int idLivro) {
        var livro = acervo.consultarLivro(idLivro).orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));
        if (livro.isExcluido()) throw new IllegalArgumentException("Livro excluído");
        obterGrupo(grupo).adicionar(new ItemLivroComposite(idLivro, livro.getTitulo()));
    }

    public boolean moverLivro(int idLivro, String origem, String destino) {
        return obterGrupo(origem).moverItemPara(new ItemLivroComposite(idLivro, ""), obterGrupo(destino));
    }

    public Map<String, List<Integer>> listarGrupos() {
        Map<String, List<Integer>> resultado = new LinkedHashMap<>();
        grupos.forEach((nome, grupo) -> resultado.put(nome, grupo.listarIdsLivros()));
        return resultado;
    }

    public int contarLivros() { return unidade.contarLivros(); }

    private GrupoAcervoComposite obterGrupo(String nome) {
        GrupoAcervoComposite grupo = grupos.get(nome);
        if (grupo == null) throw new IllegalArgumentException("Grupo não encontrado");
        return grupo;
    }
}
