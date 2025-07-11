// src/main/java/Balance_Game/Balance_Game/repository/QuestionRepository.java
package Balance_Game.Balance_Game.repository;

import Balance_Game.Balance_Game.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
}
