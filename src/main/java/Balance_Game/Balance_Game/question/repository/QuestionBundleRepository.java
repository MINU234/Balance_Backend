// src/main/java/Balance_Game/Balance_Game/repository/QuestionBundleRepository.java
package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import Balance_Game.Balance_Game.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionBundleRepository extends JpaRepository<QuestionBundle, Long>, QuestionBundleRepositoryCustom {

    // 특정 사용자가 만든 모든 질문 묶음 조회
    Page<QuestionBundle> findByCreator(User creator, Pageable pageable);

    // 공개 설정된 모든 질문 묶음 조회
    List<QuestionBundle> findByIsPublicTrue();

    /**
     * 제목 또는 설명에 검색어가 포함된 질문 묶음을 페이징 조회합니다.
     */
    @Query("SELECT qb FROM QuestionBundle qb " +
            "WHERE qb.title LIKE %:query% OR qb.description LIKE %:query% " +
            "ORDER BY qb.createdAt DESC")
    Page<QuestionBundle> findByTitleOrDescriptionContaining(
            @Param("query") String query,
            Pageable pageable
    );
    
    // 사용자별 묶음 수 카운트
    long countByCreator(User creator);
}
