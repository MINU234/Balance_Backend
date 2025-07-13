package Balance_Game.Balance_Game.game.entity;

import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_answer")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession gameSession;

    // 비회원 답변도 기록하기 위해 nullable = true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_option", nullable = false)
    private SelectedOption selectedOption; // "A" 또는 "B"

    @CreatedDate
    @Column(name = "answered_at", updatable = false, nullable = false)
    private LocalDateTime answeredAt;

    @Builder
    public UserAnswer(GameSession gameSession, User user, Question question, SelectedOption selectedOption) {
        this.gameSession = gameSession;
        this.user = user;
        this.question = question;
        this.selectedOption = selectedOption;
    }
}
