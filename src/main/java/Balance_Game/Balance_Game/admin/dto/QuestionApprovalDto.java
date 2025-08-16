package Balance_Game.Balance_Game.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuestionApprovalDto {
    private List<Long> questionIds;
}
