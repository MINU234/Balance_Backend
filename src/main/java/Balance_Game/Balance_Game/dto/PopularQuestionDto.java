package Balance_Game.Balance_Game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PopularQuestionDto {
    private Long id;
    private String text;
    private String optionAText;
    private String optionBText;
    private String creatorNickname;

    @Builder
    public PopularQuestionDto(Long id, String text, String optionAText, String optionBText, String creatorNickname) {
        this.id = id;
        this.text = text;
        this.optionAText = optionAText;
        this.optionBText = optionBText;
        this.creatorNickname = creatorNickname;
    }
}