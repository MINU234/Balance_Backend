package Balance_Game.Balance_Game.game.repository;

import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    // 공유 코드로 세션 조회
    Optional<GameSession> findByShareCode(String shareCode);
    
    // 공유 코드 존재 여부 확인
    boolean existsByShareCode(String shareCode);
    
    // 사용자별 게임 세션 조회
    Page<GameSession> findByPlayer(User player, Pageable pageable);
    
    // 부모 세션의 자식 세션들 조회
    List<GameSession> findByParentSession(GameSession parentSession);
    
    // 임시 사용자 ID로 세션 조회
    List<GameSession> findByTempUserId(String tempUserId);
    
    // 완료된 세션 조회
    @Query("SELECT gs FROM GameSession gs WHERE gs.sessionStatus = 'COMPLETED' ORDER BY gs.completedAt DESC")
    Page<GameSession> findCompletedSessions(Pageable pageable);
    
    // 특정 묶음으로 플레이한 세션 수
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.questionBundle.id = :bundleId")
    Long countByBundleId(@Param("bundleId") Long bundleId);
    
    // 사용자별 플레이 수 카운트
    long countByPlayer(User player);
    
    // 만료된 공유 코드 정리
    @Modifying
    @Query("UPDATE GameSession gs SET gs.shareCode = null, gs.shareCodeCreatedAt = null, gs.shareCodeExpiresAt = null " +
           "WHERE gs.shareCodeExpiresAt < :now AND gs.shareCode IS NOT NULL")
    int expireShareCodesOlderThan(@Param("now") LocalDateTime now);
    
    // 오래된 임시 사용자 세션의 개인정보만 익명화 (통계 데이터는 보존)
    @Modifying
    @Query("UPDATE GameSession gs SET gs.tempUserId = 'ANONYMIZED_' || gs.id " +
           "WHERE gs.tempUserId IS NOT NULL AND gs.createdAt < :cutoffDate")
    int softDeleteOldTempUserSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
