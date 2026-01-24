package Balance_Game.Balance_Game.common.controller;


import Balance_Game.Balance_Game.common.dto.KeywordStatsDto;
import Balance_Game.Balance_Game.common.service.KeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 키워드 관련 통계 정보를 제공하는 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    /**
     * 인기 키워드 목록을 조회합니다.
     *
     * @param limit 조회할 키워드 개수 (기본값: 10, 최대: 50)
     * @return ResponseEntity<List<KeywordStatsDto>> 인기 키워드 목록
     */
    @GetMapping("/popular")
    public ResponseEntity<List<KeywordStatsDto>> getPopularKeywords(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        log.info("인기 키워드 목록 조회 API 호출 - limit: {}", limit);

        try {
            List<KeywordStatsDto> keywords = keywordService.getPopularKeywords(limit);
            log.info("인기 키워드 목록 조회 API 응답 성공 - 응답 개수: {}", keywords.size());
            return ResponseEntity.ok(keywords);

        } catch (Exception e) {
            log.error("인기 키워드 목록 조회 API 오류 - limit: {}", limit, e);
            // 오류 발생 시 빈 리스트 반환하여 프론트엔드가 정상 동작하도록 함
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * 특정 키워드의 질문 개수를 조회합니다.
     *
     * @param keyword 조회할 키워드
     * @return ResponseEntity<Long> 해당 키워드를 가진 질문의 개수
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getKeywordQuestionCount(
            @RequestParam("keyword") String keyword) {

        log.info("키워드 질문 개수 조회 API 호출 - keyword: {}", keyword);

        try {
            Long count = keywordService.getKeywordQuestionCount(keyword);
            log.info("키워드 질문 개수 조회 API 응답 성공 - keyword: {}, count: {}", keyword, count);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            log.error("키워드 질문 개수 조회 API 오류 - keyword: {}", keyword, e);
            return ResponseEntity.ok(0L);
        }
    }
}