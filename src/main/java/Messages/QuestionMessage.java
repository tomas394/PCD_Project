package Messages;

import java.util.List;

public class QuestionMessage implements Message {
    private final String questionText;
    private final List<String> options;
    private final int timeLimit;
    private final boolean isTeamRound;

    public QuestionMessage(String questionText, List<String> options, int timeLimit, boolean isTeamRound) {
        this.questionText = questionText;
        this.options = options;
        this.timeLimit = timeLimit;
        this.isTeamRound = isTeamRound;
    }

    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getTimeLimit() { return timeLimit; }
    public boolean isTeamRound() { return isTeamRound; }
}