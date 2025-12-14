package Messages;
public class AnswerMessage implements Message {
    private final int answerIndex;
    public AnswerMessage(int answerIndex) { this.answerIndex = answerIndex; }
    public int getAnswerIndex() { return answerIndex; }
}