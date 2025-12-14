import Messages.Message;
import Messages.QuestionMessage;
import Messages.ScoreboardMessage;
import Messages.StatusMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameState implements Runnable {

    private final String gameCode;
    private final int maxTeams;
    private final int playersPerTeam;
    private final int numQuestions;
    private final List<Question> allQuestions;
    private Map<String, Team> teams = new HashMap<>();
    private List<Player> players = new ArrayList<>();
    private int totalPlayersReady = 0;
    private GameStatus status;
    private int currentQuestionIndex = -1;
    private Question currentQuestion;
    private boolean isRoundIndividual;
    private ModifiedCountdownLatch roundLatch;
    private Map<String, TeamBarrier> teamBarriers;
    private final Map<String, Integer> roundAnswers = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Integer> roundBonus = Collections.synchronizedMap(new HashMap<>());

    private enum GameStatus {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        FINISHED
    }

    public GameState(String gameCode, int maxTeams, int playersPerTeam, int numQuestions, List<Question> allQuestions) { //Construtor
        this.gameCode = gameCode;
        this.maxTeams = maxTeams;
        this.playersPerTeam = playersPerTeam;
        this.numQuestions = numQuestions;
        this.allQuestions = allQuestions;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
    }

    public synchronized boolean addPlayer(Player newPlayer, String teamName) { //Adiciona jogador á respetiva equipa
        if (status != GameStatus.WAITING_FOR_PLAYERS) return false;
        players.add(newPlayer);
        Team team = teams.computeIfAbsent(teamName, k -> new Team(k, playersPerTeam)); // Cria a equipa se não existir
        if (!team.addPlayer(newPlayer)) { // Verifica se a equipa está cheia
            players.remove(newPlayer);
            return false;
        }
        totalPlayersReady++;
        System.out.println("Jogador " + newPlayer.getUsername() + " entrou. (" + totalPlayersReady + "/" + (maxTeams * playersPerTeam) + ")");
        if (totalPlayersReady == (maxTeams * playersPerTeam)) {
            new Thread(this).start(); // Inicia o jogo quando todos os jogadores estiverem prontos
        }
        return true;
    }

    @Override
    public void run() {
        if (status != GameStatus.WAITING_FOR_PLAYERS) return; // Verifica o estado do jogo
        System.out.println("Jogo " + gameCode + " a começar!");
        status = GameStatus.IN_PROGRESS; //altera o estado do jogo para em progresso
        Collections.shuffle(allQuestions); //baralha as perguntas
        broadcastMessage(new StatusMessage(true, "O Jogo vai começar!"));
        try { Thread.sleep(3000); } catch (InterruptedException e) {} //espera 3 segundos antes de começar
        nextRound(); //inicia a primeira ronda
    }


    private synchronized void nextRound() {
        currentQuestionIndex++;

        roundAnswers.clear();
        roundBonus.clear();

        if (currentQuestionIndex >= numQuestions) {
            endGame();
            return;
        }

        currentQuestion = allQuestions.get(currentQuestionIndex);
        isRoundIndividual = (currentQuestionIndex % 2 == 0);
        int roundTime = 30;

        String roundType = isRoundIndividual ? "INDIVIDUAL" : "EQUIPA";
        System.out.println("Ronda " + (currentQuestionIndex + 1) + " (" + roundType + "): " + currentQuestion.getQuestion());

        int totalPlayers = players.size();
        int bonusCount = 0; // Por defeito, não há bónus
        int bonusFactor = 2;

        // Bloco de código para inicializar as condições de uma ronda do jogo,
        // distinguindo entre uma ronda individual e uma ronda em equipa.
        if (isRoundIndividual) {
            // Se a ronda for individual, define que haverá um bônus para os dois primeiros que responderem.
            bonusCount = 2; // Bónus para os primeiros 2
        } else {
            // Se a ronda for em equipa:
            teamBarriers = new HashMap<>(); // Inicializa as barreiras de equipa
            // Para cada equipa, uma barreira é configurada para esperar que todos os seus jogadores respondam.
            for (Team team : teams.values()) {
                // A ação a ser executada quando a barreira for alcançada é calcular a pontuação da equipa.
                Runnable barrierAction = () -> calculateTeamScore(team);
                // A barreira é criada com o número de jogadores da equipa e a ação de cálculo de pontuação.
                TeamBarrier barrier = new TeamBarrier(team.getPlayerCount(), barrierAction);
                teamBarriers.put(team.getTeamName(), barrier);
            }
        }

        // No final, para qualquer tipo de ronda, um `ModifiedCountdownLatch` é inicializado.
        // Este é um contador regressivo que controla o fluxo da ronda inteira (espera que todos respondam ou que o tempo se esgote).
        roundLatch = new ModifiedCountdownLatch(bonusFactor, bonusCount, roundTime, totalPlayers);

        // Lança a thread "temporizador" que vai chamar o await()
        startTimerThread(roundTime); // Passa o tempo, mas não é o principal

        // Envia a pergunta a todos os jogadores
        QuestionMessage qm = new QuestionMessage(
                currentQuestion.getQuestion(),
                currentQuestion.getOptions(),
                roundTime
        );
        broadcastMessage(qm);
    }

 
    private void startTimerThread(int seconds) {
        new Thread(() -> {
            try {

                // O await() vai desbloquear se o tempo acabar (waitPeriod)
                // OU se o count chegar a 0 (todos responderam).
                roundLatch.await();

                // Quando o await() desbloqueia (por qualquer razão), a ronda acaba.
                endRound();

            } catch (InterruptedException e) {
                System.out.println("Thread do Latch interrompida.");
            }
        }).start();
    }


    
    public void processAnswer(String username, int answerIndex) {
        if (status != GameStatus.IN_PROGRESS || isRoundOver) return;

        roundAnswers.put(username, answerIndex);

        // Notifica o Latch principal que um jogador respondeu (para controlo de tempo e bónus individual)
        if (roundLatch != null) {
            int bonus = roundLatch.countdown();
            if(isRoundIndividual) roundBonus.put(username, bonus);
        }

        // Se for uma ronda de equipa, notifica a barreira da respetiva equipa
        if (!isRoundIndividual) {
            Player p = getPlayerByUsername(username);
            if (p == null) return;

            Team t = getTeamOfPlayer(p);
            if (t != null) {
                TeamBarrier barrier = teamBarriers.get(t.getTeamName());
                if (barrier != null) {
                    barrier.playerArrived();
                }
            }
        }
    }

    private volatile boolean isRoundOver = false;

    public synchronized void endRound() {
        if (isRoundOver) return;
        isRoundOver = true;

        System.out.println("Ronda " + (currentQuestionIndex + 1) + " terminada. A calcular pontos...");

        // Em rondas de equipa, força o cálculo para as equipas que não terminaram a tempo.
        if (!isRoundIndividual) {
            for (TeamBarrier barrier : teamBarriers.values()) {
                barrier.forceCalculation();
            }
        } else {
            // Em rondas individuais, o cálculo é feito agora.
            calculateIndividualScores();
        }

        broadcastMessage(new ScoreboardMessage(getScoreboard()));

        // Prepara a próxima ronda após uma pausa
        try {
            Thread.sleep(5000); // Aumentar a pausa para dar tempo de ler o placar
        } catch (InterruptedException e) { }

        isRoundOver = false;
        nextRound();
    }

    private void calculateIndividualScores() {
        int questionPoints = currentQuestion.getPoints();
        for (Player p : players) {
            int answerIdx = roundAnswers.getOrDefault(p.getUsername(), -1);
            if (answerIdx == currentQuestion.getCorrect()) {
                int bonus = roundBonus.getOrDefault(p.getUsername(), 1);
                int score = questionPoints * bonus;
                Team t = getTeamOfPlayer(p);
                if (t != null) t.addScore(score);
            }
        }
    }



    private void calculateTeamScore(Team team) { //Calcula a pontuação da equipa após o fim da ronda. Segue as regras: dobro se todos acertarem; pontuação base se pelo menos um acertar.
        if (team == null) return;

        int questionPoints = currentQuestion.getPoints();
        boolean allCorrect = true;
        int correctAnswers = 0;

        // Itera apenas nos jogadores da equipa que efetivamente responderam
        for (Player p : team.getPlayers()) {
            if (roundAnswers.containsKey(p.getUsername())) {
                int answerIdx = roundAnswers.get(p.getUsername());
                if (answerIdx != currentQuestion.getCorrect()) {
                    allCorrect = false;
                } else {
                    correctAnswers++;
                }
            } else {
                // Se um jogador não respondeu, a equipa não pode ter o bónus de "todos acertaram"
                allCorrect = false;
            }
        }

        if (allCorrect && team.getPlayerCount() > 0) {
            // Bónus máximo (dobro) se todos acertarem
            System.out.println("Equipa " + team.getTeamName() + " recebe bónus MÁXIMO!");
            team.addScore(questionPoints * 2);
        } else if (correctAnswers > 0) {
            // Caso algum falhe, apenas será considerada a melhor pontuação de entre eles
            // Isto equivale à pontuação base da pergunta, se pelo menos um acertou.
            System.out.println("Equipa " + team.getTeamName() + " recebe pontuação base.");
            team.addScore(questionPoints);
        }
        // Se correctAnswers for 0, não adiciona pontos.
    }


  

    private synchronized void endGame() {
        if (status == GameStatus.FINISHED) return;
        status = GameStatus.FINISHED;
        isRoundOver = true; // Impede endRound de correr

        System.out.println("Jogo " + gameCode + " terminado.");
        broadcastMessage(new StatusMessage(true, "FIM DO JOGO!\n" + getScoreboard()));

        for (Player p : players) {
            p.interrupt();
        }
    }

    private void broadcastMessage(Message message) {
        for (Player p : players) {
            p.sendMessage(message);
        }
    }

    public synchronized String getScoreboard() {
        StringBuilder sb = new StringBuilder("--- PLACAR ---\n");
        for (Team team : teams.values()) {
            sb.append(team.getTeamName())
                    .append(": ")
                    .append(team.getTotalScore())
                    .append(" pts\n");
        }
        return sb.toString();
    }

    private Player getPlayerByUsername(String username) {
        for (Player p : players) {
            if (p.getUsername().equals(username)) return p;
        }
        return null;
    }

    private Team getTeamOfPlayer(Player player) {
        for (Team t : teams.values()) {
            if (t.getPlayers().contains(player)) return t;
        }
        return null;
    }
}