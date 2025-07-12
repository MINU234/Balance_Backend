package Balance_Game.Balance_Game.entity;

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

    @Builder
    public QuestionStats(Question question) {
        this.question = question;
    }
    public void incrementCount(String option) {
        if ("A".equalsIgnoreCase(option)) {
            this.optionACount++;
        } else if ("B".equalsIgnoreCase(option)) {
            this.optionBCount++;
        }
        this.totalCount++;
    }
}
