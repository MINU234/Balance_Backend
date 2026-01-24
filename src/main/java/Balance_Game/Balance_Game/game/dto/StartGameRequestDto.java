package Balance_Game.Balance_Game.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartGameRequestDto {
    @NotNull(message = "질문 묶음 ID는 필수입니다.")
    private Long bundleId;
    
    private String userEmail;     // 회원인 경우 이메일
    private String tempUserId;    // 비회원인 경우 임시 ID
    private String shareCode;     // 공유 코드로 시작하는 경우
}
