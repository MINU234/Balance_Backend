package Balance_Game.Balance_Game.question.entity;

import Balance_Game.Balance_Game.common.entity.BaseTimeEntity;
import Balance_Game.Balance_Game.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "option_a_text", nullable = false, length = 255)
    private String optionAText;

    @Column(name = "option_b_text", nullable = false, length = 255)
    private String optionBText;

    @Column(length = 50)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "option_a_image_url", length = 2048)
    private String optionAImageUrl;

    @Column(name = "option_b_image_url", length = 2048)
    private String optionBImageUrl;



    @Builder
    public Question(String text, String optionAText, String optionBText, String keyword, User creator,String optionAImageUrl,String optionBImageUrl) {
        this.text = text;
        this.optionAText = optionAText;
        this.optionBText = optionBText;
        this.keyword = keyword;
        this.creator = creator;
        this.optionAImageUrl = optionAImageUrl;
        this.optionBImageUrl = optionBImageUrl;
    }
}
