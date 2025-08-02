package Balance_Game.Balance_Game.common.service;

import Balance_Game.Balance_Game.common.dto.KeywordStatsDto;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 키워드 관련 통계 정보를 관리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final QuestionRepository questionRepository;

    /**
     * 인기 키워드 목록을 조회합니다.
     * 성능 최적화를 위해 캐싱을 적용합니다.
     *
     * @param limit 조회할 키워드 개수 (기본값: 10개)
     * @return List<KeywordStatsDto> 인기 키워드 목록
     */
    @Cacheable(value = "popularKeywords", key = "#limit", cacheManager = "cacheManager")
    public List<KeywordStatsDto> getPopularKeywords(Integer limit) {
        log.info("인기 키워드 목록 조회 시작 - limit: {}", limit);

        try {
            // limit이 null이거나 0 이하면 기본값 10 사용
            int actualLimit = (limit != null && limit > 0) ? limit : 10;

            // 최대 50개로 제한 (성능 고려)
            if (actualLimit > 50) {
                actualLimit = 50;
                log.warn("키워드 조회 개수가 50개로 제한됨. 요청된 개수: {}", limit);
            }

            Pageable pageable = PageRequest.of(0, actualLimit);
            List<KeywordStatsDto> keywords = questionRepository.findPopularKeywords(pageable);

            log.info("인기 키워드 목록 조회 완료 - 조회된 개수: {}", keywords.size());
            return keywords;

        } catch (Exception e) {
            log.error("인기 키워드 조회 중 오류 발생", e);
            return List.of(); // 빈 리스트 반환
        }
    }

    /**
     * 특정 키워드의 질문 개수를 조회합니다.
     *
     * @param keyword 조회할 키워드
     * @return Long 해당 키워드를 가진 질문의 개수
     */
    public Long getKeywordQuestionCount(String keyword) {
        log.info("키워드 질문 개수 조회 - keyword: {}", keyword);

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return 0L;
            }

            Long count = questionRepository.countByKeywordAndIsActiveTrue(keyword.trim());
            log.info("키워드 질문 개수 조회 완료 - keyword: {}, count: {}", keyword, count);

            return count != null ? count : 0L;

        } catch (Exception e) {
            log.error("키워드 질문 개수 조회 중 오류 발생 - keyword: {}", keyword, e);
            return 0L;
        }
    }
}