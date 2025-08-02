package Balance_Game.Balance_Game.common.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 홈페이지 통계 섹션에서 사용할 전체 통계 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponseDto {

    /**
     * 전체 질문 수
     */
    private Long totalQuestions;

    /**
     * 전체 질문 묶음 수
     */
    private Long totalBundles;

    /**
     * 전체 게임 플레이 횟수
     */
    private Long totalPlays;

    /**
     * 활성 사용자 수 (최근 30일 내 활동한 사용자)
     */
    private Long activeUsers;
}