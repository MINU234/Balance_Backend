package Balance_Game.Balance_Game.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionResponseDto {
    private Long sessionId;
    private Long bundleId;
    private String bundleTitle;
    private String shareCode;
    private String tempUserId;  // 비회원용 임시 ID
    
    public GameSessionResponseDto(Long sessionId) {
        this.sessionId = sessionId;
    }
}
