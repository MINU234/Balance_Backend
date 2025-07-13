package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.question.entity.QuestionStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionStatsRepository extends JpaRepository<QuestionStats, Long> {
}
