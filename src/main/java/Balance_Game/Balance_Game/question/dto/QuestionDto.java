package Balance_Game.Balance_Game.question.dto;

import Balance_Game.Balance_Game.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String text;
    private String optionAText;
    private String optionBText;
    private String keyword;
    private String optionAImageUrl;
    private String optionBImageUrl;
    private String creatorNickname;
    private boolean isActive;

    public static QuestionDto from(Question question) {
        return QuestionDto.builder()
                .id(question.getId())
                .text(question.getText())
                .optionAText(question.getOptionAText())
                .optionBText(question.getOptionBText())
                .keyword(question.getKeyword())
                .optionAImageUrl(question.getOptionAImageUrl())
                .optionBImageUrl(question.getOptionBImageUrl())
                .creatorNickname(question.getCreator() != null ?
                        question.getCreator().getNickname() : null)
                .isActive(question.isActive())
                .build();
    }
}
