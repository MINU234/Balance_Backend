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
@EntityListeners(AuditingEntityListener.class) // CreatedDate 자동 생성을 위해 추가
@Table(name = "game_session")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private QuestionBundle questionBundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_1_user_id", nullable = true)
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_2_user_id", nullable = true)
    private User player2;

    @Column(name = "session_status", nullable = false)
    private String sessionStatus; // 예: "IN_PROGRESS", "COMPLETED"

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    @Builder
    public GameSession(QuestionBundle questionBundle, User player1, User player2, String sessionStatus) {
        this.questionBundle = questionBundle;
        this.player1 = player1;
        this.player2 = player2;
        this.sessionStatus = sessionStatus;
    }
}
