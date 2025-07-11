package Balance_Game.Balance_Game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class QuestionBundleCreateRequestDto {
    private String title;
    private String description;
    private boolean isPublic;
    private List<Long> questionIds; // 묶음에 포함시킬 질문들의 ID 목록
}