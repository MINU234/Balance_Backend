package Balance_Game.Balance_Game.user.repository;

import Balance_Game.Balance_Game.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 소셜 로그인 정보로 사용자 조회
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    /**
     * 지정된 날짜 이후에 활동한 사용자 수를 조회합니다.
     * @param date 기준 날짜
     * @return 활성 사용자 수
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE u.updatedAt > :date")
    Long countActiveUsersAfter(@Param("date") LocalDateTime date);
    
    /**
     * 관리자별 활동 통계 조회
     * @return 관리자 이름, 승인 수, 거절 수, 마지막 활동 시간
     */
    @Query("SELECT u.nickname, " +
           "SUM(CASE WHEN q.approvalStatus = 'APPROVED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN q.approvalStatus = 'REJECTED' THEN 1 ELSE 0 END), " +
           "MAX(q.approvedAt) " +
           "FROM User u " +
           "LEFT JOIN Question q ON q.approvedBy = u " +
           "WHERE u.role = 'ADMIN' " +
           "GROUP BY u.nickname")
    List<Object[]> getAdminActivityStats();
}
