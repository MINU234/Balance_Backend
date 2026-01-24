package Balance_Game.Balance_Game.question.dto;

import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBundleDetailDto {
    private Long id;
    private String title;
    private String description;
    private String creatorNickname;
    private List<QuestionDto> questions;
    private List<String> keywords;

    public static QuestionBundleDetailDto from(QuestionBundle bundle) {
        return QuestionBundleDetailDto.builder()
                .id(bundle.getId())
                .title(bundle.getTitle())
                .description(bundle.getDescription())
                .creatorNickname(bundle.getCreator().getNickname())
                .questions(bundle.getQuestions().stream()
                        .map(QuestionDto::from)
                        .collect(Collectors.toList()))
                .keywords(bundle.getKeywords())
                .build();
    }
}
