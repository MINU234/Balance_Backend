package Balance_Game.Balance_Game.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageStatsDto {
    private long totalQuestions;      // 총 생성한 질문 수
    private long approvedQuestions;   // 승인된 질문 수
    private long pendingQuestions;    // 대기중인 질문 수
    private long rejectedQuestions;   // 거절된 질문 수
    private long totalBundles;        // 총 생성한 묶음 수
    private long totalGamesPlayed;    // 총 플레이한 게임 수
}
