package Balance_Game.Balance_Game.repository;

import Balance_Game.Balance_Game.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {}
