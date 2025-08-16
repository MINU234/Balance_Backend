package Balance_Game.Balance_Game.common.exception;

public class ShareCodeExpiredException extends RuntimeException {
    public ShareCodeExpiredException(String message) {
        super(message);
    }
    
    public ShareCodeExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}