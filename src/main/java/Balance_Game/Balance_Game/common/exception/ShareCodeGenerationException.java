package Balance_Game.Balance_Game.common.exception;

public class ShareCodeGenerationException extends RuntimeException {
    public ShareCodeGenerationException(String message) {
        super(message);
    }
    
    public ShareCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}