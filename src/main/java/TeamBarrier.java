import java.util.logging.Level;
import java.util.logging.Logger;

public class TeamBarrier {

    private final int totalPlayers; // Total de jogadores na equipa
    private int waitingPlayers = 0; // Jogadores que já chegaram
    private final Runnable barrierAction; // Ação a executar (cálculo de pontos)
    private boolean actionHasRun = false; // Flag para garantir que a ação só corre 1 vez
    private boolean released = false; // Flag para indicar se a barreira foi aberta

    public TeamBarrier(int totalPlayers, Runnable barrierAction) {
        this.totalPlayers = totalPlayers;
        this.barrierAction = barrierAction;
    }

    public synchronized void playerArrived() {
        if (actionHasRun || released) {
            return;
        }

        waitingPlayers++;

        if (waitingPlayers == totalPlayers) {
            // Se for o último jogador a chegar executa a ação e liberta todos
            runAction();
            released = true;
            notifyAll(); // ACORDA as threads que estão em wait()
        } else {
            // Se não for o último, espera
            while (!released && !actionHasRun) {
                try {
                    wait(); // bloqueia a thread aqui
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrompida na barreira.");
                }
            }
        }
    }

    public synchronized void forceCalculation() {
        // Força a execução se o tempo acabar (chamado pelo GameState)
        if (!actionHasRun) {
            runAction();
            released = true;
            notifyAll(); // Liberta quem ficou preso à espera
        }
    }

    private void runAction() {
        if (!actionHasRun) {
            actionHasRun = true;
            try {
                barrierAction.run();
            } catch (Exception e) {
                System.err.println("Erro ao executar a ação da barreira: " + e.getMessage());
            }
        }
    }
}