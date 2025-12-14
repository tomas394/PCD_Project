import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class KahootServer {

    private static final int DEFAULT_PORT = 8080;

    private final Map<String, GameState> activeGames = new HashMap<>();

    // Todas as perguntas carregadas do JSON
    private final List<Question> allQuestions;

    public KahootServer(List<Question> allQuestions) {
        this.allQuestions = allQuestions;
    }

    public static void main(String[] args) {
        // 1. Carregar as perguntas do JSON 
        List<Question> questions = JsonLoader.loadQuestions("questions.json");
        if (questions == null || questions.isEmpty()) {
            System.err.println("Nenhuma pergunta carregada. A sair.");
            return;
        }
        System.out.println("Perguntas carregadas com sucesso.");

        // 2. Criar o servidor
        KahootServer server = new KahootServer(questions);

        // 3. Lançar a TUI (thread para ler comandos 'new')
        server.startTUI();

        // 4. Lançar o listener de clientes (thread principal)
        server.startServer(DEFAULT_PORT);
    }

   
    public void startTUI() {
        Thread tuiThread = new Thread(() -> {
            System.out.println("Servidor TUI iniciada. Escreva 'new <equipas> <jog/equipa> <perguntas>' ou 'exit'.");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if (line.startsWith("new ")) {
                    // new <num equipas> <num jogadores por equipa> <num perguntas>
                    String[] parts = line.split(" ");
                    try {
                        int numTeams = Integer.parseInt(parts[1]);
                        int numPlayersPerTeam = Integer.parseInt(parts[2]);
                        int numQuestions = Integer.parseInt(parts[3]);
                        createGame(numTeams, numPlayersPerTeam, numQuestions);
                    } catch (Exception e) {
                        System.err.println("Comando inválido. Use: new <equipas> <jog/equipa> <perguntas>");
                    }
                } else if (line.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }
            }
        });
        tuiThread.start();
    }

    
    public void startServer(int port) { //inicia o servidor para aceitar ligações de clientes
        System.out.println("Servidor Kahoot a arrancar no porto " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Fica bloqueado aqui até um novo cliente se ligar
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente ligado de: " + clientSocket.getInetAddress());

                // Cria uma nova thread 'DealWithClient' para este cliente
                DealWithClient clientHandler = new DealWithClient(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            System.err.println("Erro no ServerSocket: " + e.getMessage());
        }
    }

    
    public synchronized void createGame(int numTeams, int numPlayersPerTeam, int numQuestions) { //cria um novo jogo e adiciona-o ao mapa de jogos ativos
        String gameCode = "Game" + activeGames.size(); 

        GameState newGame = new GameState(gameCode, numTeams, numPlayersPerTeam, numQuestions, allQuestions);

        activeGames.put(gameCode, newGame);

        System.out.println("-------------------------------------");
        System.out.println(">>> NOVO JOGO CRIADO <<<");
        System.out.println("Codigo do Jogo: " + gameCode);
        System.out.println("Equipas: " + numTeams + ", Jogadores/Equipa: " + numPlayersPerTeam);
        System.out.println("-------------------------------------");
    }

    
    public synchronized GameState getGame(String gameCode) { // premite que as threads DealWithClient acedam a um jogo de maneira segura
        return activeGames.get(gameCode);
    }
}