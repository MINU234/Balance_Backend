package Balance_Game.Balance_Game.game.entity;

import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import Balance_Game.Balance_Game.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "game_session", indexes = {
    @Index(name = "idx_share_code", columnList = "share_code"),
    @Index(name = "idx_parent_session", columnList = "parent_session_id")
})
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private QuestionBundle questionBundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_user_id")
    private User player; // 단일 플레이어로 변경

    // 8자리 공유 코드
    @Column(name = "share_code", length = 8, unique = true)
    private String shareCode;
    
    // 공유 코드 생성 시간
    @Column(name = "share_code_created_at")
    private LocalDateTime shareCodeCreatedAt;
    
    // 공유 코드 만료 시간
    @Column(name = "share_code_expires_at")
    private LocalDateTime shareCodeExpiresAt;
    
    // 부모 세션 (비교 대상)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_session_id")
    private GameSession parentSession;
    
    // 자식 세션들 (이 세션을 부모로 하는 비교 세션들)
    @OneToMany(mappedBy = "parentSession", cascade = CascadeType.ALL)
    private List<GameSession> childSessions = new ArrayList<>();

    @Column(name = "session_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus = SessionStatus.IN_PROGRESS;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // 임시 사용자 식별자 (비회원용)
    @Column(name = "temp_user_id", length = 50)
    private String tempUserId;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    @Builder
    public GameSession(QuestionBundle questionBundle, User player, String shareCode, 
                      GameSession parentSession, String tempUserId) {
        this.questionBundle = questionBundle;
        this.player = player;
        this.shareCode = shareCode;
        this.parentSession = parentSession;
        this.tempUserId = tempUserId;
        this.sessionStatus = SessionStatus.IN_PROGRESS;
    }
    
    // 게임 완료 처리
    public void complete() {
        this.sessionStatus = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    // 공유코드 설정 (만료 시간 포함)
    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
        this.shareCodeCreatedAt = LocalDateTime.now();
        this.shareCodeExpiresAt = LocalDateTime.now().plusDays(7); // 7일 후 만료
    }
    
    // 공유코드 유효성 확인
    public boolean isShareCodeValid() {
        if (this.shareCode == null || this.shareCodeExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.shareCodeExpiresAt);
    }
    
    // 공유코드 만료 처리
    public void expireShareCode() {
        this.shareCode = null;
        this.shareCodeCreatedAt = null;
        this.shareCodeExpiresAt = null;
    }
    
    // 세션 상태 enum
    public enum SessionStatus {
        IN_PROGRESS("진행중"),
        COMPLETED("완료됨"),
        EXPIRED("만료됨");
        
        private final String description;
        
        SessionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
