package Balance_Game.Balance_Game.question.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionCreateRequestDto {
    private String text;
    private String OptionAText;
    private String OptionBText;
    private String Keyword;
    private String optionAImageUrl;
    private String optionBImageUrl;
}
