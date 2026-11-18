# Cabe no Bolso — Planejador de Despesas Compartilhadas

Aplicação desktop desenvolvida em Java (Swing e arquitetura MVC) como projeto prático de **Programação Orientada a Objetos**. O sistema permite planejar despesas compartilhadas de atividades em grupo, calcular rateios fixos ou por pessoa com precisão de centavos, verificar limites de orçamento individuais, persistir dados via serialização binária e simular alternativas ótimas de corte de gastos opcionais.

## Requisitos de Ambiente
* **Versão Portátil para Windows (`CabeNoBolso-Windows.zip`):** Windows 10 ou 11 (64 bits). **Não requer Java instalado**, pois o ambiente de execução já acompanha o pacote.
* **Executável `.jar` (`cabe-no-bolso.jar`) ou compilação pelo código-fonte:** JDK / JRE 17 ou superior (testado em JDK 17 e JDK 21 LTS).
* **Dependências externas:** Nenhuma (utiliza exclusivamente bibliotecas nativas da plataforma Java SE e `javax.swing`).

## Organização das Pastas
```text
cabe-no-bolso/
├── data/
│   └── caso-referencia.dat         # Arquivo serializado pronto com o cenário de demonstração
├── docs/
│   ├── diagrama-classes.puml       # Código-fonte do diagrama UML
│   └── diagrama-classes.png        # Imagem do diagrama de classes atualizado
└── src/br/com/cabenobolso/
    ├── model/                      # Entidades, regras de negócio, rateio e otimizador de orçamento
    ├── persistence/                # Contrato PersistenciaService e serialização em arquivo
    ├── controller/                 # Controlador MVC (PlanejamentoController)
    ├── view/                       # Interface gráfica em Java Swing (MainFrame)
    └── test/                       # Bateria automatizada de testes de console
```

## Instalação e Execução Rápida (Executáveis)

Baixe a versão mais recente na aba **[Releases](https://github.com/Grupo16-POO/cabe-no-bolso/releases)** deste repositório e escolha uma das opções abaixo:

### Opção 1: Pacote Portátil para Windows (`CabeNoBolso-Windows.zip`) — Recomendado no Windows
1. Baixe o arquivo **`CabeNoBolso-Windows.zip`**.
2. Clique com o botão direito sobre o arquivo baixado e selecione **Extrair Tudo...** para descompactar a pasta `CabeNoBolso` no local de sua preferência.
3. Abra a pasta `CabeNoBolso` extraída e dê um duplo clique em **`CabeNoBolso.exe`** para iniciar o programa (não necessita instalação nem Java pré-instalado).

### Opção 2: Executável Multiplataforma (`cabe-no-bolso.jar`) — Windows, Linux e macOS
1. Certifique-se de ter o **Java 17 ou superior** instalado no computador.
2. Baixe o arquivo **`cabe-no-bolso.jar`**.
3. Dê um duplo clique sobre **`cabe-no-bolso.jar`** ou execute no terminal dentro da pasta onde o arquivo foi salvo:
   ```bash
   java -jar cabe-no-bolso.jar
   ```

## Compilação e Execução pelo Código-Fonte

1. **Clone o repositório e acesse a pasta raiz do projeto:**
   ```bash
   git clone [https://github.com/Grupo16-POO/cabe-no-bolso.git](https://github.com/Grupo16-POO/cabe-no-bolso.git)
   cd cabe-no-bolso
   ```

2. **Compile os arquivos `.java` e inicie a aplicação:**

   * **Windows (PowerShell ou CMD):**
     ```powershell
     mkdir bin -Force
     javac -encoding UTF-8 -d bin src\br\com\cabenobolso\model\*.java src\br\com\cabenobolso\persistence\*.java src\br\com\cabenobolso\controller\*.java src\br\com\cabenobolso\view\*.java src\br\com\cabenobolso\test\*.java
     java -cp bin br.com.cabenobolso.view.MainFrame
     ```

   * **Linux / macOS:**
     ```bash
     mkdir -p bin
     javac -encoding UTF-8 -d bin src/br/com/cabenobolso/model/*.java src/br/com/cabenobolso/persistence/*.java src/br/com/cabenobolso/controller/*.java src/br/com/cabenobolso/view/*.java src/br/com/cabenobolso/test/*.java
     java -cp bin br.com.cabenobolso.view.MainFrame
     ```

## Como Reproduzir os Testes

### 1. Bateria Automatizada via Console (`ValidadorRegrasTest`)
Após compilar o projeto, execute o comando abaixo na raiz `cabe-no-bolso` para validar automaticamente todos os cenários funcionais (cenário de referência, divisão de centavos `R$ 100,00 / 3`, bloqueio de entradas e vínculos inválidos, alteração de orçamento, exclusão de despesa, serialização com falha simulada de arquivo e simulador de alternativas):
```bash
java -cp bin br.com.cabenobolso.test.ValidadorRegrasTest
```

### 2. Validação Manual na Interface Gráfica (`MainFrame`)
1. Inicie a aplicação (pelo executável ou via `java -cp bin br.com.cabenobolso.view.MainFrame`).
2. Clique em **"Carregar Caso de Referência (Seção 4)"** na barra superior (ou clique em **"Carregar Planejamento (.dat)"** e abra `data/caso-referencia.dat`).
3. Na aba **3. Resumo do Planejamento**, verifique os totais individuais: **Ana R$ 75,00** (excede R$ 5,00), **Bruno R$ 60,00** (no limite), **Carla R$ 75,00** (excede R$ 5,00) e **Custo Total R$ 210,00**.
4. Para testar os demais cenários de validação:
   * **Centavos no rateio:** Cadastre uma despesa *Fixa* de `100,00` selecionando os códigos `1`, `2` e `3` e confira o rateio `R\$ 33,34`, `R\$ 33,33` e `R\$ 33,33`.
   * **Alteração de orçamento:** Na aba **1. Participantes**, selecione Ana, altere o orçamento para `80,00` e salve; na aba **3. Resumo**, o gasto permanece `R\$ 75,00` dentro do limite.
   * **Exclusão de despesa:** Com o caso original carregado, exclua *"Atividade recreativa"* na aba **2. Despesas**; todos passam a gastar `R\$ 60,00` (total `R\$ 180,00`).
   * **Persistência e falha de arquivo:** Salve em `.dat`, clique em *"Novo Planejamento (Vazio)"* e carregue o `.dat` salvo. Em seguida, tente carregar um arquivo `.txt` inválido e confirme que o planejamento em uso permanece intacto.

### 3. Simulador de Alternativas de Orçamento (Funcionalidade Extra)
* **Cenário com solução ótima:** Com o caso de referência carregado, acesse a aba **4. Simulador de Alternativas (Bônus)** e clique em **"Executar Simulação de Alternativas"**. O algoritmo retira *Sobremesa* (prioridade 1) e mantém *Atividade recreativa* (prioridade 3), reduzindo os gastos para **Ana R$ 65,00**, **Bruno R$ 50,00**, **Carla R$ 65,00** (total **R$ 180,00**) sem alterar o planejamento original.
* **Cenário sem solução:** Na aba **1. Participantes**, edite o orçamento de **Ana** para **R$ 40,00** e execute novamente o simulador na aba **4**. O sistema exibirá o alerta de inviabilidade detalhando que as despesas essenciais de Ana custam R$ 50,00 (excesso de R$ 10,00).