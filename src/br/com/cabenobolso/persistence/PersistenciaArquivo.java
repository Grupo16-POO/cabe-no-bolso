package br.com.cabenobolso.persistence;

import br.com.cabenobolso.model.Planejamento;
import java.io.*;

public class PersistenciaArquivo implements PersistenciaService {

    @Override
    public void salvar(Planejamento planejamento, String caminhoArquivo) throws IOException {
        if (planejamento == null || caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Planejamento ou caminho de arquivo inválido.");
        }
        File arquivo = new File(caminhoArquivo);
        if (arquivo.getParentFile() != null && !arquivo.getParentFile().exists()) {
            arquivo.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            oos.writeObject(planejamento);
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
                throw new IOException("O arquivo não contém um planejamento válido.");
            }
            return (Planejamento) obj;
        }
    }
}