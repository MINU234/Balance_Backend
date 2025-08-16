package Balance_Game.Balance_Game.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class GameComparisonDto {
    private Long originalSessionId;
    private Long compareSessionId;
    private String bundleTitle;
    private double matchRate;  // 일치율 (%)
    private int matchCount;     // 일치한 답변 수
    private int totalQuestions; // 전체 질문 수
    private Map<Long, AnswerComparison> comparisons;  // 질문별 비교 결과
    
    @Getter
    @Builder
    public static class AnswerComparison {
        private Long questionId;
        private String originalChoice;  // 원본 선택
        private String compareChoice;   // 비교 대상 선택
        private boolean isMatch;        // 일치 여부
    }
}
