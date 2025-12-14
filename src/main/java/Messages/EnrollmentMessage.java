package Messages;
public class EnrollmentMessage implements Message {
    private final String gameCode;
    private final String teamName;
    private final String username;
    public EnrollmentMessage(String gameCode, String teamName, String username) {
        this.gameCode = gameCode; this.teamName = teamName; this.username = username;
    }
    public String getGameCode() { return gameCode; }
    public String getTeamName() { return teamName; }
    public String getUsername() { return username; }
}