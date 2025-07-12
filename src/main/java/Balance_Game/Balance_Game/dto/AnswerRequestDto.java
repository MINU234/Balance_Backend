// src/main/java/Balance_Game/Balance_Game/service/dto/AnswerRequestDto.java
package Balance_Game.Balance_Game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class AnswerRequestDto {
    @NotNull(message = "세션 ID는 필수입니다.")
    private Long sessionId;

    @NotNull(message = "질문 ID는 필수입니다.")
    private Long questionId;

    @NotBlank(message = "선택지는 필수입니다.")
    @Pattern(regexp = "^[ABab]$", message = "선택지는 'A' 또는 'B'여야 합니다.")
    private String selectedOption;
}
