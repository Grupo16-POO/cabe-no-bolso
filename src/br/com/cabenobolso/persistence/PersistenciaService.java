package br.com.cabenobolso.persistence;

import br.com.cabenobolso.model.Planejamento;
import java.io.IOException;

public interface PersistenciaService {
    void salvar(Planejamento planejamento, String caminhoArquivo) throws IOException;
    Planejamento carregar(String caminhoArquivo) throws IOException, ClassNotFoundException;
}