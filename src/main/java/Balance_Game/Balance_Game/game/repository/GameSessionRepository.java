package Balance_Game.Balance_Game.game.repository;

import Balance_Game.Balance_Game.game.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {}
