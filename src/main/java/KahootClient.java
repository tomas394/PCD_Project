import Messages.*;

import javax.swing.SwingUtilities;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class KahootClient {

    private String host;
    private int port;
    private String gameCode;
    private String teamName;
    private String username;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private KahootClientGUI gui;

    public KahootClient(String host, int port, String gameCode, String teamName, String username) {
        this.host = host;
        this.port = port;
        this.gameCode = gameCode;
        this.teamName = teamName;
        this.username = username;
    }

    public static void main(String[] args) {

        if (args.length != 5) {
            System.err.println("Uso: java KahootClient <IP> <Porto> <Jogo> <Equipa> <Username>");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String gameCode = args[2];
            String teamName = args[3];
            String username = args[4];

            KahootClient client = new KahootClient(host, port, gameCode, teamName, username);
            client.start();

        } catch (NumberFormatException e) {
            System.err.println("Porto inválido: " + args[1]);
        }
    }

    public void start() {

        SwingUtilities.invokeLater(() -> {
            gui = new KahootClientGUI(this);
            gui.setVisible(true);
        });

        // Conectar ao servidor e fazer a inscrição
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Enviar a primeira mensagem (Inscrição)
            out.writeObject(new EnrollmentMessage(gameCode, teamName, username));
            out.flush();

            // Ler a resposta do servidor
            StatusMessage response = (StatusMessage) in.readObject();

            if (response.isSuccess()) {
                System.out.println("Servidor respondeu: " + response.getMessage());
                // Iniciar a thread que fica à escuta de mensagens do servidor
                startListenerThread();
            } else {
                System.err.println("Erro do servidor: " + response.getMessage());
                gui.showError(response.getMessage()); 
            }

        } catch (Exception e) {
            System.err.println("Erro de ligação: " + e.getMessage());
            if (gui != null) {
                gui.showError("Não foi possível ligar ao servidor.");
            }
        }
    }


    private void startListenerThread() { //thread que fica à escuta de mensagens do servidor
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    // Fica bloqueado aqui até o servidor enviar um objeto
                    Message message = (Message) in.readObject();

                    // Processa a mensagem recebida
                    processMessage(message);
                }
            } catch (Exception e) {
                System.err.println("Ligação perdida com o servidor: " + e.getMessage());
                if (gui != null) {
                    gui.showError("Ligação perdida.");
                }
            }
        });
        listenerThread.start();
    }

   
    private void processMessage(Message message) { //processa as mensagens recebidas do servidor
        // Todas as atualizações da GUI trem de usar invokeLater porque esta thread não é a thread da GUI

        if (message instanceof QuestionMessage) {
            QuestionMessage qm = (QuestionMessage) message;
            SwingUtilities.invokeLater(() -> {
                gui.atualizarPergunta(qm.getQuestionText(), qm.getOptions(), qm.getTimeLimit());
            });

        } else if (message instanceof ScoreboardMessage) {
            ScoreboardMessage sm = (ScoreboardMessage) message;
            SwingUtilities.invokeLater(() -> {
                gui.atualizarResultados(sm.getScoreboardText());
            });

        } else if (message instanceof StatusMessage) {
            StatusMessage sm = (StatusMessage) message;
            SwingUtilities.invokeLater(() -> {
                gui.showStatus(sm.getMessage());
            });
        }
    }

    // Método chamado pela GUI para enviar a resposta ao servidor
    public void sendAnswerToServer(int answerIndex) {
        try {
            System.out.println("A enviar resposta " + answerIndex + " para o servidor...");
            out.writeObject(new AnswerMessage(answerIndex));
            out.flush();
        } catch (Exception e) {
            System.err.println("Erro ao enviar resposta: " + e.getMessage());
        }
    }
}