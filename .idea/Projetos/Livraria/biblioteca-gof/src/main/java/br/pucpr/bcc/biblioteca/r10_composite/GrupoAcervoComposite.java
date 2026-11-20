package br.pucpr.bcc.biblioteca.r10_composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GrupoAcervoComposite implements ElementoAcervo {
    private final String nome;
    private final List<ElementoAcervo> filhos = new ArrayList<>();
    private final Set<Integer> idsRegistradosGlobalmente;

    public GrupoAcervoComposite(String nome, Set<Integer> idsRegistradosGlobalmente) {
        this.nome = nome;
        this.idsRegistradosGlobalmente = idsRegistradosGlobalmente;
    }

    @Override
    public String nome() { return nome; }

    public void adicionar(ElementoAcervo elemento) {
        if (elemento == this) {
            throw new IllegalArgumentException("Ciclo proibido: grupo não pode conter a si mesmo");
        }
        if (elemento instanceof GrupoAcervoComposite outroGrupo && outroGrupo.contemGrupo(this)) {
            throw new IllegalArgumentException("Ciclo proibido na hierarquia de grupos");
        }
        for (int id : elemento.listarIdsLivros()) {
            if (!idsRegistradosGlobalmente.add(id)) {
                throw new IllegalArgumentException("Livro já pertence a outro grupo do acervo: " + id);
            }
        }
        filhos.add(elemento);
    }

    public boolean moverItemPara(ItemLivroComposite item, GrupoAcervoComposite destino) {
        boolean removido = filhos.removeIf(e -> e instanceof ItemLivroComposite it && it.idLivro() == item.idLivro());
        if (!removido) return false;
        destino.filhos.add(item);
        return true;
    }

    public boolean contemGrupo(ElementoAcervo alvo) {
        if (this == alvo) return true;
        for (ElementoAcervo f : filhos) {
            if (f == alvo) return true;
            if (f instanceof GrupoAcervoComposite g && g.contemGrupo(alvo)) return true;
        }
        return false;
    }

    @Override
    public int contarLivros() {
        return filhos.stream().mapToInt(ElementoAcervo::contarLivros).sum();
    }

    @Override
    public List<Integer> listarIdsLivros() {
        List<Integer> ids = new ArrayList<>();
        for (ElementoAcervo f : filhos) {
            ids.addAll(f.listarIdsLivros());
        }
        return ids;
    }

    @Override
    public boolean contemLivro(int idLivro) {
        return filhos.stream().anyMatch(f -> f.contemLivro(idLivro));
    }
}
