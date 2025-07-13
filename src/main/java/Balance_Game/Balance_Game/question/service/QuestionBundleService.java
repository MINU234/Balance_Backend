// src/main/java/Balance_Game/Balance_Game/service/QuestionBundleService.java
package Balance_Game.Balance_Game.question.service;

import Balance_Game.Balance_Game.question.dto.PopularBundleDto;
import Balance_Game.Balance_Game.question.dto.QuestionBundleCreateRequestDto;
import Balance_Game.Balance_Game.question.entity.BundleQuestion;
import Balance_Game.Balance_Game.question.entity.Question;
import Balance_Game.Balance_Game.question.entity.QuestionBundle;
import Balance_Game.Balance_Game.question.repository.QuestionBundleRepository;
import Balance_Game.Balance_Game.question.repository.QuestionRepository;
import Balance_Game.Balance_Game.user.repository.UserRepository;
import Balance_Game.Balance_Game.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class QuestionBundleService {

    private final QuestionBundleRepository questionBundleRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createBundleByEmail(QuestionBundleCreateRequestDto requestDto, String userEmail) {
        // 1. 이메일로 사용자 정보 조회
        // UserRepository에 findByEmail 메서드가 정의되어 있어야 합니다.
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("요청한 이메일에 해당하는 사용자를 찾을 수 없습니다."));

        // 2. 질문 묶음(QuestionBundle) 엔티티 생성
        QuestionBundle questionBundle = QuestionBundle.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .isPublic(requestDto.isPublic())
                .creator(creator)
                .build();

        // 3. 요청된 질문 ID 목록으로 실제 Question 엔티티들을 조회
        List<Question> questions = questionRepository.findAllById(requestDto.getQuestionIds());

        // 요청된 ID 개수와 실제 조회된 엔티티 개수가 다르면, 유효하지 않은 ID가 포함된 것이므로 예외 처리
        if (questions.size() != requestDto.getQuestionIds().size()) {
            throw new IllegalArgumentException("존재하지 않는 질문 ID가 포함되어 있습니다.");
        }

        // 4. 묶음과 질문의 연결 정보(BundleQuestion)를 생성하고 순서를 지정
        IntStream.range(0, questions.size())
                .forEach(i -> {
                    Question question = questions.get(i);
                    BundleQuestion bundleQuestion = BundleQuestion.builder()
                            .questionBundle(questionBundle)
                            .question(question)
                            .orderIndex(i) // DTO로 받은 리스트의 순서를 그대로 저장
                            .build();

                    // QuestionBundle의 편의 메서드를 통해 연관관계 설정
                    questionBundle.getBundleQuestions().add(bundleQuestion);
                });

        // 5. QuestionBundle 저장
        // @OneToMany의 cascade = CascadeType.ALL 옵션 덕분에 BundleQuestion 엔티티들도 함께 저장됩니다.
        QuestionBundle savedBundle = questionBundleRepository.save(questionBundle);

        return savedBundle.getId();
    }

    @Transactional(readOnly = true)
    public Page<PopularBundleDto> getPopularBundles(Pageable pageable) {
        // 기존과 동일하게 Repository의 메서드를 호출합니다.
        // Spring Data JPA가 알아서 QueryDSL 구현체를 찾아 실행해 줍니다.
        return questionBundleRepository.findPopularBundles(pageable);
    }

    // [참고] 플레이 횟수 증가 로직 예시 (GamePlayService 등에 위치)
    @Transactional
    public void incrementPlayCount(Long bundleId) {
        QuestionBundle bundle = questionBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 묶음입니다."));
        bundle.getStats().incrementPlayCount();
    }
}
