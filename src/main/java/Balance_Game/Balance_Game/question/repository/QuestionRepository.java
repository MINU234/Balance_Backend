package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.common.dto.KeywordStatsDto;
import Balance_Game.Balance_Game.question.entity.ApprovalStatus;
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    Page<Question> findByCreator(User creator, Pageable pageable);
    
    // 승인 상태별 질문 조회
    Page<Question> findByApprovalStatus(ApprovalStatus status, Pageable pageable);
    
    // 특정 상태가 아닌 질문 조회
    Page<Question> findByApprovalStatusNot(ApprovalStatus status, Pageable pageable);
    
    // 사용자별 승인 대기 질문 조회
    Page<Question> findByCreatorAndApprovalStatus(User creator, ApprovalStatus status, Pageable pageable);
    
    // 승인된 질문만 조회 (일반 사용자용)
    @Query("SELECT q FROM Question q WHERE q.approvalStatus = 'APPROVED' AND q.isActive = true")
    Page<Question> findApprovedQuestions(Pageable pageable);
    
    // 키워드별 승인된 질문 조회
    @Query("SELECT q FROM Question q WHERE q.keyword = :keyword AND q.approvalStatus = 'APPROVED' AND q.isActive = true")
    Page<Question> findApprovedQuestionsByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 사용자별 질문 수 카운트
    long countByCreator(User creator);
    
    // 사용자별 승인 상태별 질문 수 카운트
    long countByCreatorAndApprovalStatus(User creator, ApprovalStatus status);
    
    // 승인 상태별 전체 카운트
    long countByApprovalStatus(ApprovalStatus status);
    
    // 특정 시간 이후 생성된 질문 수
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    // 일별 승인 상태 통계
    @Query("SELECT DATE(q.createdAt), " +
           "SUM(CASE WHEN q.approvalStatus = 'PENDING' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN q.approvalStatus = 'APPROVED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN q.approvalStatus = 'REJECTED' THEN 1 ELSE 0 END) " +
           "FROM Question q " +
           "WHERE q.createdAt >= :startDate " +
           "GROUP BY DATE(q.createdAt) " +
           "ORDER BY DATE(q.createdAt) DESC")
    List<Object[]> getDailyStatsByApprovalStatus(@Param("startDate") LocalDateTime startDate);
    
    // 키워드별 승인 상태 통계
    @Query("SELECT q.keyword, COUNT(q) " +
           "FROM Question q " +
           "WHERE q.approvalStatus = :status AND q.keyword IS NOT NULL " +
           "GROUP BY q.keyword " +
           "ORDER BY COUNT(q) DESC")
    List<Object[]> getKeywordStatsByApprovalStatus(@Param("status") ApprovalStatus status);
}
