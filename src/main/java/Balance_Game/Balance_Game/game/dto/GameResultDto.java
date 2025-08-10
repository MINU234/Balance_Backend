package Balance_Game.Balance_Game.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultDto {
    private Long sessionId;
    private String bundleTitle;
    private String shareCode;  // 공유 코드 추가
    private Map<Long, String> userChoices; // questionId -> selectedOption
    private Integer totalQuestions;
}
