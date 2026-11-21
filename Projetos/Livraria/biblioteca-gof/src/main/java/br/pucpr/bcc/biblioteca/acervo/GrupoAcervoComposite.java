package br.pucpr.bcc.biblioteca.acervo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Objects;

public class GrupoAcervoComposite implements ElementoAcervo {
    private final String nome;
    private GrupoAcervoComposite pai;
    private final List<ElementoAcervo> filhos = new ArrayList<>();
    private final Set<Integer> idsRegistradosGlobalmente;

    public GrupoAcervoComposite(String nome, Set<Integer> idsRegistradosGlobalmente) {
        this.nome = nome;
        this.idsRegistradosGlobalmente = idsRegistradosGlobalmente;
    }

    @Override
    public String nome() { return nome; }

    public void adicionar(ElementoAcervo elemento) {
        Objects.requireNonNull(elemento);
        if (elemento == this || elemento instanceof GrupoAcervoComposite g && g.contemGrupo(this)) {
            throw new IllegalArgumentException("Ciclo proibido na hierarquia de grupos");
        }
        if (elemento instanceof GrupoAcervoComposite grupo) {
            if (grupo.pai != null) throw new IllegalArgumentException("Grupo já possui pai");
            if (grupo.idsRegistradosGlobalmente != idsRegistradosGlobalmente) {
                throw new IllegalArgumentException("Grupos devem compartilhar o registro do acervo");
            }
            // As folhas já foram registradas ao entrar na seção. Anexar a seção
            // não representa uma segunda inclusão de seus livros.
            grupo.pai = this;
        } else if (elemento instanceof ItemLivroComposite item) {
            if (!idsRegistradosGlobalmente.add(item.idLivro())) {
                throw new IllegalArgumentException("Livro já pertence a outro grupo: " + item.idLivro());
            }
        } else {
            throw new IllegalArgumentException("Elemento de acervo não suportado");
        }
        filhos.add(elemento);
    }

    public boolean moverItemPara(ItemLivroComposite item, GrupoAcervoComposite destino) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(destino);
        if (destino.idsRegistradosGlobalmente != idsRegistradosGlobalmente) {
            throw new IllegalArgumentException("Transferência exige o mesmo acervo");
        }
        for (int i = 0; i < filhos.size(); i++) {
            ElementoAcervo atual = filhos.get(i);
            if (atual instanceof ItemLivroComposite folha && folha.idLivro() == item.idLivro()) {
                if (destino == this) return true;
                destino.filhos.add(atual);
                filhos.remove(i);
                return true;
            }
        }
        return false;
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
