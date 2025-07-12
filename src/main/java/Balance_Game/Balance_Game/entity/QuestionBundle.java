package Balance_Game.Balance_Game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "question_bundle")
public class QuestionBundle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bundle_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @OneToMany(mappedBy = "questionBundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BundleQuestion> bundleQuestions = new ArrayList<>();

    @Builder
    public QuestionBundle(String title, String description, User creator, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.isPublic = isPublic;
    }

    @Column(length = 255)
    private String keywords; // 쉼표로 구분된 키워드 문자열 (예: "#우정,#친구")

    @OneToOne(mappedBy = "questionBundle", cascade = CascadeType.ALL)
    private QuestionBundleStats stats;

    // 통계 엔티티를 초기화하는 편의 메서드
    @PostPersist
    public void createStats() {
        this.stats = QuestionBundleStats.builder().questionBundle(this).build();
    }
}
