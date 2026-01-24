package Balance_Game.Balance_Game.game.repository;

import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.game.entity.UserAnswer;
import Balance_Game.Balance_Game.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    // 게임 세션별 답변 조회 (N+1 문제 해결을 위한 Fetch Join)
    @Query("SELECT ua FROM UserAnswer ua " +
           "JOIN FETCH ua.question " +
           "WHERE ua.gameSession.id = :sessionId " +
           "ORDER BY ua.question.id")
    List<UserAnswer> findByGameSessionId(@Param("sessionId") Long sessionId);
    
    // 성능 최적화: 게임 세션과 질문을 모두 fetch join
    @Query("SELECT ua FROM UserAnswer ua " +
           "JOIN FETCH ua.question q " +
           "JOIN FETCH ua.gameSession gs " +
           "WHERE ua.gameSession.id = :sessionId " +
           "ORDER BY q.id")
    List<UserAnswer> findByGameSessionIdWithDetails(@Param("sessionId") Long sessionId);
    
    // 중복 답변 체크
    boolean existsByGameSessionAndQuestion(GameSession gameSession, Question question);
    
    // 특정 질문에 대한 답변 통계
    @Query("SELECT ua.selectedOption, COUNT(ua) FROM UserAnswer ua " +
           "WHERE ua.question.id = :questionId " +
           "GROUP BY ua.selectedOption")
    List<Object[]> getAnswerStatsByQuestion(@Param("questionId") Long questionId);
}
