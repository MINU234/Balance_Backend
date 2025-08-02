package Balance_Game.Balance_Game.question.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 검색 결과 및 인기 묶음 응답 DTO
@Getter
@Builder
public class PopularBundleDto {

    private Long id;
    private String title;
    private String description;
    private String creatorNickname;  // author → creatorNickname
    private int playCount;           // plays → playCount
    private int questionCount;       // questions → questionCount
    private List<String> keywords;
}
