package Balance_Game.Balance_Game.repository;

import Balance_Game.Balance_Game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 소셜 로그인 정보로 사용자 조회
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
}
