package Balance_Game.Balance_Game.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionBundleStats {

    @Id
    private Long id; // QuestionBundle의 ID와 동일한 값을 사용합니다.

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 이 필드를 기본 키로 매핑합니다.
    @JoinColumn(name = "bundle_id")
    private QuestionBundle questionBundle;

    @Column(nullable = false)
    private long playCount = 0L; // 플레이 횟수

    @Builder
    public QuestionBundleStats(QuestionBundle questionBundle) {
        this.questionBundle = questionBundle;
    }

    // 플레이 횟수를 증가시키는 편의 메서드
    public void incrementPlayCount() {
        this.playCount++;
    }
}
