package Balance_Game.Balance_Game.question.entity;

import Balance_Game.Balance_Game.common.entity.BaseTimeEntity;
import Balance_Game.Balance_Game.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    // 승인 상태 관리를 위한 필드 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;



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
    
    // 질문 승인 메서드
    public void approve(User admin) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = admin;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }
    
    // 질문 거절 메서드
    public void reject(User admin, String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvedBy = admin;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    // 승인된 질문인지 확인
    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }
}
