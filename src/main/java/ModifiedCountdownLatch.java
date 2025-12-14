
public class ModifiedCountdownLatch {

    private int count; // O número de jogadores que falta responder
    private int bonusCount; // O número de bónus (ex: 2)
    private final int bonusFactor; // O multiplicador (ex: 2, para o dobro)
    private final long waitPeriod; // Tempo limite em milissegundos

    private boolean timedOut = false; // Flag para indicar se o tempo expirou

    public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int count) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod * 1000L; // convertido para milissegundos
        this.count = count;
    }

   
    public synchronized void await() throws InterruptedException { // espera até o contador chegar a 0 ou o tempo expirar
        long startTime = System.currentTimeMillis();
        long remainingTime = waitPeriod;

        // Continua a esperar enquanto o contador for > 0 E o tempo não tiver expirado
        while (count > 0 && remainingTime > 0) {
            wait(remainingTime); // Espera (pode ser acordado por notifyAll ou timeout)

            // Recalcula o tempo que falta
            remainingTime = waitPeriod - (System.currentTimeMillis() - startTime);
        }

        // Se saimos do loop e o contador > 0, foi porque o tempo expirou
        if (count > 0 && remainingTime <= 0) {
            timedOut = true;
        }

        // Acorda todas as threads 'countdown' que possam estar à espera
        notifyAll();
    }

    
    public synchronized int countdown() { // decrementa o contador e retorna o fator de pontuação a aplicar
        // Se a ronda já acabou (por tempo ou contagem), não dá bónus
        if (count == 0 || timedOut) {
            return 1; // sem bónus
        }

        // Decrementa o contador de jogadores
        count--;

        int factorToApply = 1; // Fator padrão (sem bónus)
        
        // Se houver bónus disponíveis, aplica um
        if (bonusCount > 0) {
            factorToApply = bonusFactor; // Aplica o bónus
            bonusCount--; // Gasta um bónus
        }

        // Se este foi o último jogador a responder,
        // acorda a thread que está em 'await()'
        if (count == 0) {
            notifyAll();
        }

        return factorToApply;
    }
}
