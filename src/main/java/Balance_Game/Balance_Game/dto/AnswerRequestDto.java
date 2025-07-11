package Balance_Game.Balance_Game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequestDto {
    private Long questionId;
    private String selectedOption; // "A" 또는 "B"
}
