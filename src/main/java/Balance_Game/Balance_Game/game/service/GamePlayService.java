package Balance_Game.Balance_Game.game.service;

import Balance_Game.Balance_Game.common.util.ShareCodeGenerator;
import Balance_Game.Balance_Game.common.constants.GameConstants;
import Balance_Game.Balance_Game.game.dto.GameResultDto;
import Balance_Game.Balance_Game.game.dto.GameComparisonDto;
import Balance_Game.Balance_Game.game.entity.GameSession;
import Balance_Game.Balance_Game.game.entity.SelectedOption;
import Balance_Game.Balance_Game.game.entity.UserAnswer;
import Balance_Game.Balance_Game.game.repository.GameSessionRepository;
import Balance_Game.Balance_Game.game.repository.UserAnswerRepository;
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import Balance_Game.Balance_Game.question.entity.QuestionBundleStats;
import Balance_Game.Balance_Game.question.entity.QuestionStats;
import Balance_Game.Balance_Game.question.repository.QuestionBundleRepository;
import Balance_Game.Balance_Game.question.repository.QuestionBundleStatsRepository;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.question.repository.QuestionStatsRepository;
import Balance_Game.Balance_Game.game.dto.AnswerRequestDto;
import Balance_Game.Balance_Game.game.dto.GameSessionResponseDto;
import Balance_Game.Balance_Game.game.dto.StartGameRequestDto;
import Balance_Game.Balance_Game.user.entity.User;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionBundleRepository questionBundleRepository;
    private final QuestionBundleStatsRepository questionBundleStatsRepository;
    private final QuestionStatsRepository questionStatsRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ShareCodeGenerator shareCodeGenerator;

    /**
     * 새로운 게임 세션 시작 (개인 플레이)
     */
    @Transactional
    public GameSessionResponseDto startGame(StartGameRequestDto requestDto) {
        // 1. QuestionBundle 조회
        QuestionBundle bundle = questionBundleRepository.findById(requestDto.getBundleId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 질문 묶음을 찾을 수 없습니다: " + requestDto.getBundleId()));

        // 2. 사용자 정보 조회 (선택적)
        User player = null;
        if (requestDto.getUserEmail() != null) {
            player = userRepository.findByEmail(requestDto.getUserEmail()).orElse(null);
        }
        
        // 3. 임시 사용자 ID 생성 (비회원용)
        String tempUserId = null;
        if (player == null && requestDto.getTempUserId() != null) {
            tempUserId = requestDto.getTempUserId();
        } else if (player == null) {
            tempUserId = UUID.randomUUID().toString();
        }

        // 4. 부모 세션 조회 (공유 코드로 플레이하는 경우)
        GameSession parentSession = null;
        if (requestDto.getShareCode() != null) {
            parentSession = gameSessionRepository.findByShareCode(requestDto.getShareCode())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 공유 코드입니다."));
        }

        // 5. 새로운 GameSession 생성
        GameSession newSession = GameSession.builder()
                .questionBundle(bundle)
                .player(player)
                .tempUserId(tempUserId)
                .parentSession(parentSession)
                .build();
        
        GameSession savedSession = gameSessionRepository.save(newSession);

        // 6. 통계 업데이트
        QuestionBundleStats bundleStats = questionBundleStatsRepository.findById(bundle.getId())
                .orElseGet(() -> {
                    // @Builder를 사용하여 생성
                    QuestionBundleStats newStats = QuestionBundleStats.builder()
                            .questionBundle(bundle)
                            .build();
                    return questionBundleStatsRepository.save(newStats);
                });
        bundleStats.incrementPlayCount();

        // 7. 응답 생성
        GameSessionResponseDto response = new GameSessionResponseDto(savedSession.getId());
        response.setTempUserId(tempUserId);
        
        log.info("게임 세션 생성: sessionId={}, bundleId={}, player={}", 
                savedSession.getId(), bundle.getId(), player != null ? player.getEmail() : "비회원");

        return response;
    }
    
    /**
     * 고유한 공유 코드 생성 (개선된 알고리즘)
     */
    private String generateUniqueShareCode() {
        String shareCode;
        int attempts = 0;
        
        do {
            // 타임스탬프 기반 코드 먼저 시도
            shareCode = shareCodeGenerator.generateShareCode();
            attempts++;
            
            // 일정 횟수 실패 시 순수 랜덤 코드로 전환
            if (attempts > GameConstants.MAX_SHARE_CODE_ATTEMPTS / 2) {
                shareCode = shareCodeGenerator.generateRandomShareCode();
            }
            
            if (attempts > GameConstants.MAX_SHARE_CODE_ATTEMPTS) {
                // 최종 실패 시 현재 시간 기반 고유 코드 생성
                shareCode = "ERR" + String.valueOf(System.currentTimeMillis()).substring(8, 13);
                log.warn("공유 코드 생성 최대 시도 횟수 초과, 임시 코드 사용: {}", shareCode);
                break;
            }
        } while (gameSessionRepository.existsByShareCode(shareCode));
        
        return shareCode;
    }

    /**
     * 사용자 답변 기록
     */
    @Transactional
    public void recordAnswer(AnswerRequestDto requestDto) {
        // 1. 관련 엔티티 조회
        GameSession session = gameSessionRepository.findById(requestDto.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 게임 세션 ID입니다: " + requestDto.getSessionId()));

        Question question = questionRepository.findById(requestDto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 질문을 찾을 수 없습니다: " + requestDto.getQuestionId()));

        // 2. 선택지 검증 및 변환
        SelectedOption selectedEnumOption;
        try {
            selectedEnumOption = SelectedOption.valueOf(requestDto.getSelectedOption().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택지는 'A' 또는 'B'여야 합니다.");
        }

        // 3. 중복 답변 체크
        boolean alreadyAnswered = userAnswerRepository.existsByGameSessionAndQuestion(session, question);
        if (alreadyAnswered) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 답변한 질문입니다.");
        }

        // 4. UserAnswer 저장
        UserAnswer userAnswer = UserAnswer.builder()
                .gameSession(session)
                .question(question)
                .selectedOption(selectedEnumOption)
                .build();
        userAnswerRepository.save(userAnswer);

        // 5. QuestionStats 업데이트
        QuestionStats questionStats = questionStatsRepository.findById(requestDto.getQuestionId())
                .orElseGet(() -> {
                    // @Builder를 사용하여 생성
                    QuestionStats newStats = QuestionStats.builder()
                            .question(question)
                            .build();
                    return questionStatsRepository.save(newStats);
                });
        questionStats.incrementCount(selectedEnumOption);
        
        log.debug("답변 기록: sessionId={}, questionId={}, selection={}", 
                requestDto.getSessionId(), requestDto.getQuestionId(), selectedEnumOption);
    }

    /**
     * 게임 완료 및 공유 코드 생성
     */
    @Transactional
    public String completeGameAndGetShareCode(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        // 이미 완료된 세션인지 확인
        if (session.getSessionStatus() == GameSession.SessionStatus.COMPLETED) {
            return session.getShareCode();
        }

        // 게임 완료 처리
        session.complete();

        // 개선된 공유 코드 생성 (타임스탬프 기반으로 충돌 확률 최소화)
        String shareCode = generateUniqueShareCode();
        session.setShareCode(shareCode);
        gameSessionRepository.save(session);

        log.info("게임 완료 및 공유 코드 생성: sessionId={}, shareCode={}", sessionId, shareCode);
        return shareCode;
    }

    /**
     * 게임 결과 조회
     */
    @Transactional(readOnly = true)
    public GameResultDto getGameResults(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        List<UserAnswer> answers = userAnswerRepository.findByGameSessionId(sessionId);

        Map<Long, String> userChoices = answers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer.getSelectedOption().name()
                ));

        return GameResultDto.builder()
                .sessionId(sessionId)
                .bundleTitle(session.getQuestionBundle().getTitle())
                .shareCode(session.getShareCode())
                .userChoices(userChoices)
                .totalQuestions(session.getQuestionBundle().getQuestionCount())
                .build();
    }

    /**
     * 공유 코드로 결과 비교
     */
    @Transactional(readOnly = true)
    public GameComparisonDto compareResults(String shareCode, Long compareSessionId) {
        // 원본 세션 조회
        GameSession originalSession = gameSessionRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 공유 코드입니다."));
        
        // 공유 코드 만료 체크
        if (!originalSession.isShareCodeValid()) {
            throw new IllegalArgumentException("만료된 공유 코드입니다.");
        }

        // 비교 세션 조회
        GameSession compareSession = gameSessionRepository.findById(compareSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비교 세션입니다."));

        // 같은 묶음인지 확인
        if (!originalSession.getQuestionBundle().getId().equals(compareSession.getQuestionBundle().getId())) {
            throw new IllegalArgumentException("다른 질문 묶음으로는 비교할 수 없습니다.");
        }

        // 답변 조회 (성능 최적화: Fetch Join 사용)
        List<UserAnswer> originalAnswers = userAnswerRepository.findByGameSessionIdWithDetails(originalSession.getId());
        List<UserAnswer> compareAnswers = userAnswerRepository.findByGameSessionIdWithDetails(compareSessionId);

        // 일치율 계산
        int matchCount = 0;
        Map<Long, GameComparisonDto.AnswerComparison> comparisons = new HashMap<>();

        for (UserAnswer originalAnswer : originalAnswers) {
            Long questionId = originalAnswer.getQuestion().getId();
            
            UserAnswer compareAnswer = compareAnswers.stream()
                    .filter(a -> a.getQuestion().getId().equals(questionId))
                    .findFirst()
                    .orElse(null);

            if (compareAnswer != null) {
                boolean isMatch = originalAnswer.getSelectedOption() == compareAnswer.getSelectedOption();
                if (isMatch) matchCount++;

                comparisons.put(questionId, GameComparisonDto.AnswerComparison.builder()
                        .questionId(questionId)
                        .originalChoice(originalAnswer.getSelectedOption().name())
                        .compareChoice(compareAnswer.getSelectedOption().name())
                        .isMatch(isMatch)
                        .build());
            }
        }

        double matchRate = originalAnswers.isEmpty() ? 0 : 
                (double) matchCount / originalAnswers.size() * 100;

        return GameComparisonDto.builder()
                .originalSessionId(originalSession.getId())
                .compareSessionId(compareSessionId)
                .bundleTitle(originalSession.getQuestionBundle().getTitle())
                .matchRate(matchRate)
                .matchCount(matchCount)
                .totalQuestions(originalAnswers.size())
                .comparisons(comparisons)
                .build();
    }

    /**
     * 공유 코드로 세션 정보 조회
     */
    @Transactional(readOnly = true)
    public GameSessionResponseDto getSessionByShareCode(String shareCode) {
        GameSession session = gameSessionRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 공유 코드입니다."));
        
        // 공유 코드 만료 체크
        if (!session.isShareCodeValid()) {
            throw new IllegalArgumentException("만료된 공유 코드입니다.");
        }

        GameSessionResponseDto response = new GameSessionResponseDto(session.getId());
        response.setBundleId(session.getQuestionBundle().getId());
        response.setBundleTitle(session.getQuestionBundle().getTitle());
        response.setShareCode(shareCode);
        
        return response;
    }
}
