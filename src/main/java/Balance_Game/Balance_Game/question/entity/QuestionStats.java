package Balance_Game.Balance_Game.question.entity;

import Balance_Game.Balance_Game.game.entity.SelectedOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "question_stats")
public class QuestionStats {

    @Id
    @Column(name = "question_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "option_a_count", nullable = false)
    private long optionACount = 0;

    @Column(name = "option_b_count", nullable = false)
    private long optionBCount = 0;

    @Column(name = "total_count", nullable = false)
    private long totalCount = 0;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성자에 @Builder 적용 (권장 방식)
    @Builder
    public QuestionStats(Question question) {
        this.question = question;
        this.id = question.getId(); // MapsId 사용시 필요
    }

    public void incrementCount(SelectedOption option) {
        if (option == SelectedOption.A) {
            this.optionACount++;
        } else if (option == SelectedOption.B) {
            this.optionBCount++;
        }
        this.totalCount++;
    }
    
    // setter 메서드는 제거 (불필요)
    // JPA가 내부적으로 리플렉션을 사용하므로 setter 없이도 동작
}
