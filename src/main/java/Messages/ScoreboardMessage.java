package Messages;
public class ScoreboardMessage implements Message {
    private final String scoreboardText;
    public ScoreboardMessage(String scoreboardText) { this.scoreboardText = scoreboardText; }
    public String getScoreboardText() { return scoreboardText; }
}