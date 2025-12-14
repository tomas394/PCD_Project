Este projeto é uma implementação de um jogo de perguntas e respostas multijogador (semelhante ao Kahoot) desenvolvido em Java. O sistema utiliza uma arquitetura Cliente-Servidor e foca-se no uso de programação concorrente (Threads, Sincronização, Locks, Barreiras e Latches) para gerir múltiplos jogos e jogadores em simultâneo.

📋 Pré-requisitos
Java JDK 8 ou superior instalado.

Terminal/Linha de comandos.

🛠️ Como Compilar
Antes de executar, é necessário compilar todos os ficheiros .java para gerar as classes (.class).

Abra o terminal na pasta raiz do projeto e execute:

Bash

javac *.java
🚀 Como Executar
O sistema funciona em duas partes: primeiro inicia-se o Servidor, cria-se um jogo, e depois iniciam-se os Clientes.

1. Iniciar o Servidor
Certifique-se de que o ficheiro questions.json está na mesma pasta.

Execute o servidor:

Bash

java KahootServer
O servidor ficará à espera de comandos. Para criar um novo jogo, use o comando new com a seguinte sintaxe: new <num_equipas> <jogadores_por_equipa> <num_perguntas>

Exemplo (cria um jogo para 2 equipas, 2 pessoas cada, 5 perguntas):

Plaintext

new 2 2 5
O servidor irá confirmar a criação e mostrar o Código do Jogo (ex: Game0). Anote este código, os clientes vão precisar dele.

2. Iniciar os Clientes (Jogadores)
Abra novos terminais (um para cada jogador) e execute o cliente.

Sintaxe: java KahootClient <host> <porta> <codigo_jogo> <nome_equipa> <username>

host: localhost (se for no mesmo PC).

porta: 8080 (porta padrão do servidor).

codigo_jogo: O código gerado pelo servidor (ex: Game0).

nome_equipa: A equipa onde quer entrar (ex: EquipaA).

username: O seu nome único.

Exemplo Prático:

Jogador 1 (Ana):

Bash

java KahootClient localhost 8080 Game0 EquipaA Ana
Jogador 2 (João):

Bash

java KahootClient localhost 8080 Game0 EquipaA Joao
🎮 Regras e Pontuação
O jogo inicia automaticamente quando todas as equipas estiverem cheias.

Tipos de Perguntas
O jogo alterna entre dois tipos de rondas:

Rondas Individuais:

Cada jogador joga por si, mas os pontos somam para a equipa.

Bónus de Rapidez: Os primeiros 2 jogadores a responder corretamente ganham o dobro dos pontos.

Usa um CountdownLatch modificado para controlar a rapidez.

Rondas de Equipa:

A equipa precisa de se coordenar.

A pontuação só é atribuída quando todos os membros da equipa responderem.

Se todos acertarem: Pontuação a dobrar para a equipa.

Se apenas alguns acertarem: Pontuação normal.

Usa Barreiras (Wait/NotifyAll) para sincronizar a equipa.

Temporizador
Cada pergunta tem um limite de tempo (ex: 30 segundos).

Se o tempo acabar, a ronda termina e quem não respondeu não pontua.

📂 Estrutura do Projeto
KahootServer: Classe principal do servidor. Gere o ServerSocket e a TUI.

KahootClient: Classe principal do cliente. Trata da rede e inicia a GUI.

KahootClientGUI: Interface gráfica (Swing) para o jogador.

GameState: O "cérebro" do jogo. Gere a lógica, pontuações e fluxo das rondas.

DealWithClient: Thread no servidor que gere a ligação com um único jogador.

ModifiedCountdownLatch: Ferramenta de sincronização customizada para rondas individuais.

TeamBarrier: Ferramenta de sincronização para rondas de equipa.

Messages (Package): Classes de mensagens (AnswerMessage, QuestionMessage, etc.) para comunicação.
