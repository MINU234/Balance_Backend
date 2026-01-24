package Balance_Game.Balance_Game.question.repository;

import Balance_Game.Balance_Game.question.entity.QuestionBundleStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionBundleStatsRepository extends JpaRepository<QuestionBundleStats, Long> {
    @Query("SELECT COALESCE(SUM(qbs.playCount), 0) FROM QuestionBundleStats qbs")
    Long sumAllPlayCounts();
}
