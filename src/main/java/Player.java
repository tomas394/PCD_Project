
import Messages.Message;

import java.io.ObjectOutputStream;

public class Player {
    private String username;
    private transient ObjectOutputStream out;
    private transient DealWithClient handler;

    public Player(String username, ObjectOutputStream out) {
        this.username = username;
        this.out = out;
    }

    public String getUsername() {
        return username;
    }

    
    public synchronized void sendMessage(Message message) { 
        if (out == null) return;
        try {
            out.writeObject(message); // Envia a mensagem
            out.flush(); // Garante que a mensagem é enviada agora
        } catch (Exception e) {
            System.err.println("Falha ao enviar mensagem para " + username + ": " + e.getMessage());
        }
    }

    public void setHandler(DealWithClient handler) {
        this.handler = handler;
    }
    public void interrupt() {
        if (handler != null) {
            handler.interruptThread();
        }
    }
}