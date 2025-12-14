import Messages.AnswerMessage;
import Messages.EnrollmentMessage;
import Messages.StatusMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class DealWithClient implements Runnable {

    private final Socket socket;
    private final KahootServer server;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private String username;
    private GameState game;

    private Thread myThread;

    public DealWithClient(Socket socket, KahootServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void interruptThread() {
        if (myThread != null) {
            myThread.interrupt();
        }
    }
 //Faz todo o trabalho de comunicação com o cliente
    @Override
    public void run() {
        myThread = Thread.currentThread();

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Object msg = in.readObject();
            if (!(msg instanceof EnrollmentMessage)) {
                sendError("Protocolo inválido. Esperava EnrollmentMessage.");
                return;
            }

            EnrollmentMessage enrollment = (EnrollmentMessage) msg;
            this.username = enrollment.getUsername();
            this.game = server.getGame(enrollment.getGameCode());

            if (game == null) {
                sendError("Jogo '" + enrollment.getGameCode() + "' não encontrado.");
                return;
            }

            Player player = new Player(username, out);
            player.setHandler(this);

            if (!game.addPlayer(player, enrollment.getTeamName())) {
                sendError("Falha ao entrar no jogo. Username duplicado ou jogo/equipa cheia.");
                return;
            }

            out.writeObject(new StatusMessage(true, "Inscrito! A aguardar..."));
            out.flush();

            // Loop principal de escuta de respostas
            while (!Thread.currentThread().isInterrupted()) {
                Object answerMsg = in.readObject();
                if (answerMsg instanceof AnswerMessage) {
                    System.out.println("Servidor recebeu resposta de: " + username);
                    game.processAnswer(username, ((AnswerMessage)answerMsg).getAnswerIndex());
                }
            }


        } catch (SocketException | java.io.EOFException e) {
            // A interrupção (endGame) ou um "disconnect" do cliente
            // vai causar esta exceção.
            System.out.println("Ligação com " + (username != null ? username : "") + " terminada.");
        } catch (Exception e) {
            System.err.println("Erro na thread DealWithClient (" + username + "): " + e.getMessage());
            e.printStackTrace(); // Útil para debugging
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (Exception e) { /* ignora */ }
        }
    }

    
    private void sendError(String errorMessage) { //envia uma mensagem de erro ao cliente e fecha a ligação
        try {
            out.writeObject(new StatusMessage(false, errorMessage));
            out.flush();
        } catch (Exception e) {
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
}