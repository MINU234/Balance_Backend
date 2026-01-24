package Balance_Game.Balance_Game.common.exception;

public class GameSessionNotFoundException extends RuntimeException {
    public GameSessionNotFoundException(String message) {
        super(message);
    }
    
    public GameSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}