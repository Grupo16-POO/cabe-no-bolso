package br.com.cabenobolso.persistence;

import br.com.cabenobolso.model.Planejamento;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistenciaArquivo implements PersistenciaService {

    @Override
    public void salvar(Planejamento planejamento, String caminhoArquivo) throws IOException {
        if (planejamento == null) {
            throw new IllegalArgumentException("Nenhum planejamento ativo para salvar.");
        }
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O caminho do arquivo não pode ficar vazio.");
        }

        File arquivo = new File(caminhoArquivo);
        File pastaPai = arquivo.getParentFile();
        if (pastaPai != null && !pastaPai.exists()) {
            pastaPai.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            oos.writeObject(planejamento);
            oos.flush();
        }
    }

    @Override
    public Planejamento carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            throw new IllegalArgumentException("O caminho do arquivo não pode ficar vazio.");
        }

        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + caminhoArquivo);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object obj = ois.readObject();
            if (!(obj instanceof Planejamento)) {
                throw new IOException("O arquivo selecionado não contém um planejamento válido.");
            }
            return (Planejamento) obj;
        }
    }
}