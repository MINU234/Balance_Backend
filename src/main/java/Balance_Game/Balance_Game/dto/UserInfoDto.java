package Balance_Game.Balance_Game.dto;

import Balance_Game.Balance_Game.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoDto {
    private String email;
    private String nickname;

    public static UserInfoDto from(User user) {
        return UserInfoDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
