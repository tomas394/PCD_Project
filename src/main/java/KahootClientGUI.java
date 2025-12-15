import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Timer;


public class KahootClientGUI extends JFrame {

    private static final String PAINEL_ESPERA = "ESPERA";
    private static final String PAINEL_PERGUNTA = "PERGUNTA";
    private static final String PAINEL_RESULTADOS = "RESULTADOS";

    private CardLayout cardLayout;
    private JPanel painelPrincipal;
    private JLabel labelTimer;
    private JLabel labelPergunta;
    private JButton[] botoesResposta = new JButton[4];
    private JTextArea areaResultados;
    private JLabel labelEspera;
    private KahootClient client;
    private JLabel teamRoundLabel; // Label para indicar ronda de equipa


    private Timer swingTimer; // O temporizador do Swing
    private int tempoRestante; // contador

    public KahootClientGUI(KahootClient client) {
        this.client = client;
        setTitle("IsKahoot Cliente");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);

        painelPrincipal.add(criarPainelEspera(), PAINEL_ESPERA);
        painelPrincipal.add(criarPainelPergunta(), PAINEL_PERGUNTA);
        painelPrincipal.add(criarPainelResultados(), PAINEL_RESULTADOS);

        add(painelPrincipal);
        mostrarEcra(PAINEL_ESPERA);
    }

  
    public void atualizarPergunta(String pergunta, List<String> opcoes, int tempo, boolean isTeamRound) {
        // 1. Atualiza a pergunta e opções de resposta
        labelPergunta.setText("<html><div style='text-align: center;'>" + pergunta + "</div></html>");
        for (int i = 0; i < 4; i++) {
            if (i < opcoes.size()) {
                botoesResposta[i].setText(opcoes.get(i));
                botoesResposta[i].setEnabled(true);
            } else {
                botoesResposta[i].setText("");
                botoesResposta[i].setEnabled(false);
            }
        }

        // Mostra ou esconde a label de ronda de equipa
        teamRoundLabel.setVisible(isTeamRound);


        // 2. Para qualquer temporizador antigo que possa estar a correr
        if (swingTimer != null) {
            swingTimer.stop();
        }

        // 3. Define o tempo inicial
        this.tempoRestante = tempo;
        labelTimer.setText("Tempo: " + tempoRestante);

        // 4. Cria um novo ActionListener para o tick do temporizador
        ActionListener timerAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tempoRestante--; // Decrementa o tempo

                if (tempoRestante >= 0) {
                    labelTimer.setText("Tempo: " + tempoRestante);
                }

                if (tempoRestante <= 0) {
                    swingTimer.stop(); // Para o temporizador quando
                }
            }
        };

        // 5. Cria e inicia o novo temporizador
        swingTimer = new Timer(1000, timerAction);
        swingTimer.start();

        // 6. Mostra a tela da pergunta
        mostrarEcra(PAINEL_PERGUNTA);
    }



    private JPanel criarPainelEspera() {
        JPanel painel = new JPanel(new GridBagLayout());
        labelEspera = new JLabel("A ligar ao servidor...");
        labelEspera.setFont(new Font("Arial", Font.BOLD, 24));
        painel.add(labelEspera);
        return painel;
    }

    private JPanel criarPainelPergunta() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Painel Norte com Timer e Label de Ronda de Equipa
        JPanel northPanel = new JPanel(new BorderLayout());
        labelTimer = new JLabel("Tempo: 30", SwingConstants.CENTER);
        labelTimer.setFont(new Font("Arial", Font.BOLD, 20));
        northPanel.add(labelTimer, BorderLayout.NORTH);

        teamRoundLabel = new JLabel("Ronda de Equipa", SwingConstants.CENTER);
        teamRoundLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        teamRoundLabel.setForeground(Color.BLUE);
        teamRoundLabel.setVisible(false); // Inicialmente escondido
        northPanel.add(teamRoundLabel, BorderLayout.CENTER);

        painel.add(northPanel, BorderLayout.NORTH);

        labelPergunta = new JLabel("Aqui vai aparecer a pergunta...", SwingConstants.CENTER);
        labelPergunta.setFont(new Font("Arial", Font.PLAIN, 28));
        painel.add(labelPergunta, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < 4; i++) {
            botoesResposta[i] = new JButton("Opção " + (i + 1));
            botoesResposta[i].setFont(new Font("Arial", Font.BOLD, 18));
            final int indiceResposta = i;
            botoesResposta[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enviarResposta(indiceResposta);
                }
            });
            painelBotoes.add(botoesResposta[i]);
        }
        painel.add(painelBotoes, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarPainelResultados() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titulo = new JLabel("Resultados da Ronda", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        painel.add(titulo, BorderLayout.NORTH);
        areaResultados = new JTextArea("O placar será mostrado aqui...\n");
        areaResultados.setFont(new Font("Monospaced", Font.PLAIN, 16));
        areaResultados.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaResultados);
        painel.add(scrollPane, BorderLayout.CENTER);
        return painel;
    }

    public void mostrarEcra(String nomeTela) {
        cardLayout.show(painelPrincipal, nomeTela);
    }

    public void atualizarResultados(String placar) {
        areaResultados.setText(placar);
        mostrarEcra(PAINEL_RESULTADOS);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void showStatus(String message) {
        labelEspera.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
        mostrarEcra(PAINEL_ESPERA);
    }

    private void enviarResposta(int indice) {
        System.out.println("DEBUG: Resposta " + indice + " selecionada.");
        for (JButton button : botoesResposta) {
            button.setEnabled(false);
        }
        client.sendAnswerToServer(indice);
    }
}
