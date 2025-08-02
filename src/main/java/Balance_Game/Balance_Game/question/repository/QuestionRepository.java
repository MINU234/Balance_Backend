// src/main/java/Balance_Game/Balance_Game/repository/QuestionRepository.java
package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.common.dto.KeywordStatsDto;
import Balance_Game.Balance_Game.question.entity.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
    @Query("SELECT COUNT(q) FROM Question q WHERE q.isActive = true")
    Long countByIsActiveTrue();

    /**
     * 인기 키워드 목록을 조회합니다. (키워드별 질문 개수 기준 내림차순)
     * @param pageable 페이징 정보
     * @return 인기 키워드 통계 목록
     */
    @Query("SELECT new Balance_Game.Balance_Game.common.dto.KeywordStatsDto(q.keyword, COUNT(q)) " +
            "FROM Question q " +
            "WHERE q.keyword IS NOT NULL AND q.keyword != '' AND q.isActive = true " +
            "GROUP BY q.keyword " +
            "ORDER BY COUNT(q) DESC")
    List<KeywordStatsDto> findPopularKeywords(Pageable pageable);

    /**
     * 특정 키워드를 가진 활성화된 질문의 개수를 조회합니다.
     * @param keyword 검색할 키워드
     * @return 해당 키워드를 가진 질문 개수
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.keyword = :keyword AND q.isActive = true")
    Long countByKeywordAndIsActiveTrue(@Param("keyword") String keyword);
}
