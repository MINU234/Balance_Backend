package Balance_Game.Balance_Game.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bundle_question", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bundle_question", columnNames = {"bundle_id", "question_id"})
})
public class BundleQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bundle_question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private QuestionBundle questionBundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Builder
    public BundleQuestion(QuestionBundle questionBundle, Question question, int orderIndex) {
        this.questionBundle = questionBundle;
        this.question = question;
        this.orderIndex = orderIndex;
    }
}
