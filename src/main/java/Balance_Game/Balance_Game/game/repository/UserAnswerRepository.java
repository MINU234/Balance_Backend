package Balance_Game.Balance_Game.game.repository;

import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.game.entity.UserAnswer;
import Balance_Game.Balance_Game.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    // 게임 세션별 답변 조회
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.gameSession.id = :sessionId")
    List<UserAnswer> findByGameSessionId(@Param("sessionId") Long sessionId);
    
    // 중복 답변 체크
    boolean existsByGameSessionAndQuestion(GameSession gameSession, Question question);
    
    // 특정 질문에 대한 답변 통계
    @Query("SELECT ua.selectedOption, COUNT(ua) FROM UserAnswer ua " +
           "WHERE ua.question.id = :questionId " +
           "GROUP BY ua.selectedOption")
    List<Object[]> getAnswerStatsByQuestion(@Param("questionId") Long questionId);
}
