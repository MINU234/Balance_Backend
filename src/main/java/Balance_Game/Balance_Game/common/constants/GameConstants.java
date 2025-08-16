package Balance_Game.Balance_Game.common.constants;

public class GameConstants {
    // 공유코드 관련 상수
    public static final int SHARE_CODE_LENGTH = 8;
    public static final int SHARE_CODE_EXPIRE_DAYS = 3;
    public static final int MAX_SHARE_CODE_ATTEMPTS = 50; // 100 -> 50으로 줄임
    public static final String SHARE_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    // JWT 관련 상수
    public static final long ACCESS_TOKEN_EXPIRE_MINUTES = 30;
    public static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;
    
    // 게임 관련 상수
    public static final int MAX_QUESTIONS_PER_BUNDLE = 50;
    public static final int MAX_BUNDLE_TITLE_LENGTH = 100;
    public static final int MAX_QUESTION_TEXT_LENGTH = 200;
    
    // 페이징 관련 상수
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    private GameConstants() {
        // 인스턴스 생성 방지
    }
}