// src/main/java/Balance_Game/Balance_Game/repository/UserAnswerRepository.java
package Balance_Game.Balance_Game.game.repository;

import Balance_Game.Balance_Game.game.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
}

