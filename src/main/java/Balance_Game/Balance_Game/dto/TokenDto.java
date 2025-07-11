package Balance_Game.Balance_Game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDto {
    private String grantType; // "Bearer"
    private String accessToken;
}
