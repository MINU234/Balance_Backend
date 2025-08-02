package Balance_Game.Balance_Game.user.repository;

import Balance_Game.Balance_Game.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
}
