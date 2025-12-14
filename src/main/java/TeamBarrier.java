
public class TeamBarrier {

    private final int totalPlayers; // Total de jogadores na equipa
    private int waitingPlayers = 0; // Jogadores que já chegaram
    private final Runnable barrierAction; // Ação a executar (cálculo de pontos)
    private boolean actionHasRun = false; // Flag para garantir que a ação só corre 1 vez


    public TeamBarrier(int totalPlayers, Runnable barrierAction) {
        this.totalPlayers = totalPlayers;
        this.barrierAction = barrierAction;
    }

 
    public synchronized void playerArrived() { // chamado quando um jogador chega à barreira
        if (actionHasRun) {
            return;
        }

        waitingPlayers++;
        if (waitingPlayers == totalPlayers) {
            runAction();
        }
    }

    public synchronized void forceCalculation() { //Força a execução da ação da barreira. Usado em caso de timeout da ronda. Garante que a pontuação é calculada mesmo que nem todos os jogadores respondam.
        runAction();
    }

    
    private void runAction() { //Executa a ação da barreira, garantindo que só corre uma vez.
        if (!actionHasRun) {
            actionHasRun = true;
            try {
                // Executa o código que foi passado (ex: calcular a pontuação da equipa)
                barrierAction.run();
            } catch (Exception e) {
                System.err.println("Erro ao executar a ação da barreira: " + e.getMessage());
            }
        }
    }
}