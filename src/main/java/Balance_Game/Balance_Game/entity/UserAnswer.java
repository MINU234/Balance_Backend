package Balance_Game.Balance_Game.entity;

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
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_option", nullable = false, length = 1)
    private SelectedOption selectedOption;

    @CreatedDate
    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    @Builder
    public UserAnswer(User user, Question question, SelectedOption selectedOption) {
        this.user = user;
        this.question = question;
        this.selectedOption = selectedOption;
    }
}
