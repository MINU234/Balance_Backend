package Balance_Game.Balance_Game.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인기 키워드 통계 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordStatsDto {

    /**
     * 키워드 이름
     */
    private String name;

    /**
     * 해당 키워드를 가진 질문의 개수
     */
    private Long count;
}