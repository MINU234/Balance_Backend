package Balance_Game.Balance_Game.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StartGameRequestDto {
    @NotNull(message = "질문 묶음 ID는 필수입니다.")
    private Long bundleId;
}
