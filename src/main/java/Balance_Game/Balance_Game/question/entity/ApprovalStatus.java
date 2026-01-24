package Balance_Game.Balance_Game.question.entity;

/**
 * 질문 승인 상태를 나타내는 Enum
 */
public enum ApprovalStatus {
    PENDING("대기중"),     // 승인 대기
    APPROVED("승인됨"),    // 승인됨
    REJECTED("거절됨");    // 거절됨
    
    private final String description;
    
    ApprovalStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
