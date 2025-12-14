import java.util.ArrayList;
import java.util.List;

public class Team {
    private String teamName;
    private int totalScore;
    private List<Player> players;
    private int maxPlayers;

    public Team(String teamName, int maxPlayers) {
        this.teamName = teamName;
        this.maxPlayers = maxPlayers;
        this.totalScore = 0;
        this.players = new ArrayList<>();
    }

    public synchronized boolean addPlayer(Player player) {
        if (players.size() < maxPlayers) {
            players.add(player);
            return true;
        }
        return false;
    }

    public String getTeamName() { return teamName; }
    public int getTotalScore() { return totalScore; }

    public synchronized void addScore(int points) {
        this.totalScore += points;
    }

    public synchronized List<Player> getPlayers() { return players; }
    public synchronized int getPlayerCount() { return players.size(); }
}