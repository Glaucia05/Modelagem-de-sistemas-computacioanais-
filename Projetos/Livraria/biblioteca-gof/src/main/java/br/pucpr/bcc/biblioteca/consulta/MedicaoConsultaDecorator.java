package br.pucpr.bcc.biblioteca.consulta;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MedicaoConsultaDecorator extends ConsultaAcervoDecorator {
    private final AtomicInteger totalMedicoes = new AtomicInteger(0);

    public MedicaoConsultaDecorator(ConsultaAcervoService proximo) {
        super(proximo);
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        totalMedicoes.incrementAndGet();
        return proximo.consultarMetadados(idLivro);
    }

    @Override
    public boolean podeReservar(int idLivro) {
        totalMedicoes.incrementAndGet();
        return proximo.podeReservar(idLivro);
    }

    public int getTotalMedicoes() {
        return totalMedicoes.get();
    }
}
