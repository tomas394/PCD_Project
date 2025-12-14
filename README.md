# 🧠 IsKahoot - Projeto de Programação Concorrente e Distribuida

O **IsKahoot** é um jogo de perguntas e respostas multijogador inspirado no Kahoot, desenvolvido em **Java** com arquitetura **Cliente-Servidor** e um forte foco em **programação concorrente**.



---

## 📋 Pré-requisitos

- Java JDK 8 ou superior instalado
- Terminal ou linha de comandos

---

## 🛠️ Como Compilar

Antes de executar o sistema, é necessário compilar todos os ficheiros `.java` para gerar os ficheiros `.class`.

Abra o terminal na pasta raiz do projeto e execute:

```bash
javac *.java
```

---

## 🚀 Como Executar

### 1️⃣ Iniciar o Servidor

Certifique-se de que o ficheiro `questions.json` está na mesma pasta do servidor.

Execute o servidor com:

```bash
java KahootServer
```

O servidor ficará à espera de comandos no terminal.

Para criar um novo jogo, utilize o comando `new` com a seguinte sintaxe:

```
new <num_equipas> <jogadores_por_equipa> <num_perguntas>
```

**Exemplo:**
Criar um jogo com:
- 2 equipas
- 2 jogadores por equipa  
- 5 perguntas

```
new 2 2 5
```

O servidor irá confirmar a criação do jogo e apresentar um **Código do Jogo**, por exemplo:

```
Game0
```

⚠️ **Este código deve ser anotado** - os clientes precisam dele para entrar no jogo.

### 2️⃣ Iniciar os Clientes (Jogadores)

Abra um novo terminal para cada jogador.

A sintaxe de execução do cliente é:

```bash
java KahootClient <host> <porta> <codigo_jogo> <nome_equipa> <username>
```

**Parâmetros:**
- `host`: Endereço do servidor (use `localhost` se estiver no mesmo computador)
- `porta`: Porta do servidor (por defeito 8080)
- `codigo_jogo`: Código fornecido pelo servidor (exemplo: `Game0`)
- `nome_equipa`: Nome da equipa onde o jogador vai entrar (exemplo: `EquipaA`)
- `username`: Nome único do jogador

**Exemplo Prático:**

**Jogador 1 – Ana:**
```bash
java KahootClient localhost 8080 Game0 EquipaA Ana
```

**Jogador 2 – João:**
```bash
java KahootClient localhost 8080 Game0 EquipaA Joao
```

O jogo inicia automaticamente assim que todas as equipas estiverem completas - sem botões, sem contagens decrescentes dramáticas.

---

## 🎮 Regras e Pontuação

O jogo alterna entre dois tipos de rondas: **Rondas Individuais** e **Rondas de Equipa**.

### 🧍 Rondas Individuais
- Cada jogador responde individualmente
- Os pontos obtidos somam para a pontuação da equipa
- **Bónus de Rapidez**: Os primeiros 2 jogadores a responder corretamente recebem o dobro dos pontos

**Implementação Concorrente:**
- Utiliza um `ModifiedCountdownLatch`
- Permite identificar quem responde primeiro
- Controla o fecho da ronda quando todos respondem ou o tempo termina
- É basicamente uma corrida - quem carrega primeiro ganha mais

### 👥 Rondas de Equipa
- Todos os membros da equipa devem responder
- A pontuação só é atribuída quando todos tiverem submetido resposta

**Regras de Pontuação:**
- Todos acertam → pontuação a dobrar para a equipa
- Apenas alguns acertam → pontuação normal

**Implementação Concorrente:**
- Utiliza barreiras baseadas em `wait()` e `notifyAll()`
- Garante que a equipa avança em conjunto ou ninguém avança
- Aqui ninguém fica para trás - ou ganham todos ou ninguém se arma em herói

### ⏱️ Temporizador
- Cada pergunta tem um limite de tempo (por exemplo, 30 segundos)
- Quando o tempo termina:
  - A ronda é encerrada
  - Jogadores que não responderam não recebem pontos
- O relógio não perdoa

---

## 📂 Estrutura do Projeto

### 🖥️ Servidor
- **KahootServer**: Classe principal do servidor. Gere o ServerSocket e a interface de texto (TUI)
- **DealWithClient**: Thread do servidor responsável por um único jogador. Gere a comunicação cliente-servidor
- **GameState**: O cérebro do sistema. Controla o estado do jogo, perguntas, rondas, pontuações e sincronização

### 🧰 Concorrência e Sincronização
- **ModifiedCountdownLatch**: Versão adaptada de um CountdownLatch. Utilizada nas rondas individuais para controlar rapidez e término da ronda
- **TeamBarrier**: Barreira de sincronização para rondas de equipa. Garante que todos os membros respondem antes de continuar

### 💻 Cliente
- **KahootClient**: Classe principal do cliente. Gere a ligação ao servidor
- **KahootClientGUI**: Interface gráfica desenvolvida em Swing. Permite ao jogador responder às perguntas

### 📩 Comunicação (Package Messages)
- Conjunto de classes de mensagens (exemplo: `QuestionMessage`, `AnswerMessage`) utilizadas para a comunicação entre cliente e servidor

---

## 🎯 Funcionalidades Principais

- ✅ Suporte para múltiplos jogos simultâneos
- ✅ Sistema de equipas com diferentes modos de jogo
- ✅ Temporizador por pergunta
- ✅ Sistema de pontuação com bónus por rapidez
- ✅ Interface gráfica intuitiva para os jogadores
- ✅ Sincronização robusta usando mecanismos concorrentes
- ✅ Comunicação cliente-servidor eficiente

---

## 📝 Notas de Desenvolvimento

Este projeto foi desenvolvido como exercício académico para demonstrar:
- Programação concorrente em Java
- Design de sistemas distribuídos
- Sincronização entre múltiplas threads
- Comunicação cliente-servidor usando sockets
- Desenvolvimento de interfaces gráficas com Swing

---

## 👥 Autores

Projeto desenvolvido no âmbito da disciplina de Programação Concorrente e Distribuída.
Tomás Carlos - 123304

