// src/main/java/Balance_Game/Balance_Game/repository/QuestionBundleRepository.java
package Balance_Game.Balance_Game.repository;

import Balance_Game.Balance_Game.entity.QuestionBundle;
import Balance_Game.Balance_Game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionBundleRepository extends JpaRepository<QuestionBundle, Long>, QuestionBundleRepositoryCustom {

    // 특정 사용자가 만든 모든 질문 묶음 조회
    List<QuestionBundle> findByCreator(User creator);

    // 공개 설정된 모든 질문 묶음 조회
    List<QuestionBundle> findByIsPublicTrue();
}
