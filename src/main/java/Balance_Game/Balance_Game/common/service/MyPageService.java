package Balance_Game.Balance_Game.common.service;

import Balance_Game.Balance_Game.common.dto.MyPageStatsDto;
import Balance_Game.Balance_Game.game.dto.GameResultDto;
import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.game.repository.GameSessionRepository;
import Balance_Game.Balance_Game.question.dto.QuestionDto;
import Balance_Game.Balance_Game.question.entity.ApprovalStatus;
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.question.repository.QuestionBundleRepository;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.user.entity.User;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuestionBundleRepository questionBundleRepository;
    private final GameSessionRepository gameSessionRepository;
    
    /**
     * 내 질문 승인 상태별 조회
     */
    public Page<QuestionDto> getMyQuestionsByStatus(String email, ApprovalStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return questionRepository.findByCreatorAndApprovalStatus(user, status, pageable)
                .map(QuestionDto::from);
    }
    
    /**
     * 내 게임 기록 조회
     */
    public Page<GameResultDto> getMyGameHistory(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return gameSessionRepository.findByPlayer(user, pageable)
                .map(this::convertToGameResultDto);
    }
    
    /**
     * 내 통계 조회
     */
    public MyPageStatsDto getMyStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 통계 계산
        long totalQuestions = questionRepository.countByCreator(user);
        long approvedQuestions = questionRepository.countByCreatorAndApprovalStatus(user, ApprovalStatus.APPROVED);
        long pendingQuestions = questionRepository.countByCreatorAndApprovalStatus(user, ApprovalStatus.PENDING);
        long rejectedQuestions = questionRepository.countByCreatorAndApprovalStatus(user, ApprovalStatus.REJECTED);
        long totalBundles = questionBundleRepository.countByCreator(user);
        long totalGamesPlayed = gameSessionRepository.countByPlayer(user);
        
        return MyPageStatsDto.builder()
                .totalQuestions(totalQuestions)
                .approvedQuestions(approvedQuestions)
                .pendingQuestions(pendingQuestions)
                .rejectedQuestions(rejectedQuestions)
                .totalBundles(totalBundles)
                .totalGamesPlayed(totalGamesPlayed)
                .build();
    }
    
    private GameResultDto convertToGameResultDto(GameSession session) {
        return GameResultDto.builder()
                .sessionId(session.getId())
                .bundleTitle(session.getQuestionBundle().getTitle())
                .shareCode(session.getShareCode())
                .totalQuestions(session.getQuestionBundle().getQuestionCount())
                .build();
    }
}
