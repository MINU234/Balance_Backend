package Balance_Game.Balance_Game.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDto {
    private String grantType; // "Bearer"
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // 초 단위
    
    public static TokenDto of(String accessToken, String refreshToken, Long expiresIn) {
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
