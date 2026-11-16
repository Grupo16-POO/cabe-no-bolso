# Cabe no Bolso — Planejador de Despesas Compartilhadas

Aplicação desktop desenvolvida em Java (Swing e arquitetura MVC) como atividade final da disciplina de **Programação Orientada a Objetos**. O sistema permite planejar despesas compartilhadas de atividades em grupo, calcular rateios fixos ou por pessoa com precisão de centavos, verificar limites de orçamento individuais, persistir dados via serialização binária e simular alternativas ótimas de corte de gastos opcionais.

## Requisitos de Ambiente
* **JDK:** Java 17 ou superior (testado em JDK 17 e JDK 21 LTS).
* **Dependências externas:** Nenhuma (utiliza exclusivamente bibliotecas nativas da plataforma Java SE e `javax.swing`).

## Organização das Pastas
```text
cabe-no-bolso/
├── data/
│   └── caso-referencia.dat         # Arquivo serializado pronto com o caso da Seção 4
├── docs/
│   ├── diagrama-classes.puml       # Código-fonte do diagrama UML
│   └── diagrama-classes.png        # Imagem do diagrama de classes atualizado
└── src/br/com/cabenobolso/
    ├── model/                      # Entidades, regras de negócio, rateio e simulador (bônus)
    ├── persistence/                # Contrato PersistenciaService e serialização em arquivo
    ├── controller/                 # Controlador MVC (PlanejamentoController)
    ├── view/                       # Interface gráfica em Java Swing (MainFrame)
    └── test/                       # Bateria automatizada de testes de console
```

## Instalação, Compilação e Execução

1. **Clone o repositório e acesse a pasta raiz do projeto:**
```bash
git clone [https://github.com/Grupo16-POO/cabe-no-bolso.git](https://github.com/Grupo16-POO/cabe-no-bolso.git)
cd cabe-no-bolso
```

2. **Compile e execute pela linha de comando:**

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
Após compilar o projeto, execute o comando abaixo na raiz `cabe-no-bolso` para validar automaticamente todos os cenários obrigatórios (caso de referência, divisão de centavos `R$ 100,00 / 3`, bloqueio de entradas e vínculos inválidos, alteração de orçamento, exclusão de despesa, serialização com falha simulada de arquivo e simulador de alternativas):
```bash
java -cp bin br.com.cabenobolso.test.ValidadorRegrasTest
```

### 2. Validação Manual na Interface Gráfica (`MainFrame`)
1. Inicie a aplicação (`java -cp bin br.com.cabenobolso.view.MainFrame`).
2. Clique em **"Carregar Caso de Referência (Seção 4)"** na barra superior (ou clique em **"Carregar Planejamento (.dat)"** e abra `data/caso-referencia.dat`).
3. Na aba **3. Resumo do Planejamento**, verifique os totais individuais: **Ana R$ 75,00** (excede R$ 5,00), **Bruno R$ 60,00** (no limite), **Carla R$ 75,00** (excede R$ 5,00) e **Custo Total R$ 210,00**.
4. Para testar os demais cenários da Seção 4:
   * **Centavos no rateio:** Cadastre uma despesa *Fixa* de `100,00` selecionando os códigos `1`, `2` e `3` e confira o rateio `R\$ 33,34`, `R\$ 33,33` e `R\$ 33,33`.
   * **Alteração de orçamento:** Na aba **1. Participantes**, selecione Ana, altere o orçamento para `80,00` e salve; na aba **3. Resumo**, o gasto permanece `R\$ 75,00` dentro do limite.
   * **Exclusão de despesa:** Com o caso original carregado, exclua *"Atividade recreativa"* na aba **2. Despesas**; todos passam a gastar `R\$ 60,00` (total `R\$ 180,00`).
   * **Persistência e falha de arquivo:** Salve em `.dat`, clique em *"Novo Planejamento (Vazio)"* e carregue o `.dat` salvo. Em seguida, tente carregar um arquivo `.txt` inválido e confirme que o planejamento em uso permanece intacto.

### 3. Simulador de Alternativas (Objetivo Extra — Seção 7)
* **Cenário com solução ótima:** Com o caso de referência carregado, acesse a aba **4. Simulador de Alternativas (Bônus)** e clique em **"Executar Simulação de Alternativas"**. O algoritmo retira *Sobremesa* (prioridade 1) e mantém *Atividade recreativa* (prioridade 3), reduzindo os gastos para **Ana R$ 65,00**, **Bruno R$ 50,00**, **Carla R$ 65,00** (total **R$ 180,00**) sem alterar o planejamento original.
* **Cenário sem solução:** Na aba **1. Participantes**, edite o orçamento de **Ana** para **R$ 40,00** e execute novamente o simulador na aba **4**. O sistema exibirá o alerta de inviabilidade detalhando que as despesas essenciais de Ana custam R$ 50,00 (excesso de R$ 10,00).