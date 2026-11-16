# Cabe no Bolso — Planejador de Despesas Compartilhadas

Aplicação desktop desenvolvida em Java (Swing e arquitetura MVC) como atividade final da disciplina de **Programação Orientada a Objetos**. O sistema permite planejar despesas compartilhadas de atividades em grupo, calcular rateios fixos ou por pessoa com precisão de centavos, verificar limites de orçamento individuais, persistir dados via serialização binária e simular alternativas ótimas de corte de gastos opcionais.

## Requisitos de Ambiente
* **JDK:** Java 17 ou superior (testado em JDK 17 e JDK 21 LTS).
* **Dependências externas:** Nenhuma (utiliza exclusivamente bibliotecas nativas da plataforma Java SE e `javax.swing`).

## Organização do Projeto
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