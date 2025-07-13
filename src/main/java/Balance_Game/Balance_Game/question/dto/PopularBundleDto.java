package Balance_Game.Balance_Game.question.dto;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class PopularBundleDto {
    private Long id;
    private String title;
    private String description;
    private String creatorNickname;
    private Long playCount;
    private Integer questionCount;
    private List<String> keywords;

    // JPQL의 new 생성자 표현식을 위한 생성자
    public PopularBundleDto(Long id, String title, String description, String creatorNickname, Long playCount, Integer questionCount, String keywords) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorNickname = creatorNickname;
        this.playCount = (playCount == null) ? 0L : playCount; // 통계 정보가 없을 경우 0으로 초기화
        this.questionCount = questionCount;
        if (keywords != null && !keywords.isEmpty()) {
            this.keywords = Arrays.asList(keywords.split(","));
        }
    }
}
