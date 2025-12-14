package Messages;
public class StatusMessage implements Message {
    private final String message;
    private final boolean success;
    public StatusMessage(boolean success, String message) { this.success = success; this.message = message; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
}