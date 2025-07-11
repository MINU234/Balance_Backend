// src/main/java/Balance_Game/Balance_Game/controller/QuestionBundleController.java
package Balance_Game.Balance_Game.controller;

import Balance_Game.Balance_Game.dto.QuestionBundleCreateRequestDto;
import Balance_Game.Balance_Game.service.QuestionBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-bundles")
public class QuestionBundleController {

    private final QuestionBundleService questionBundleService;

    // 새로운 질문 묶음 생성 (POST /api/question-bundles)
    @PostMapping
    public ResponseEntity<Long> createBundle(
            @RequestBody QuestionBundleCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 현재 로그인한 사용자의 정보를 기반으로 서비스 호출
        // 서비스 계층에서는 이메일 대신 Long 타입의 ID를 사용할 수도 있습니다.
        // 이 경우 CustomUserDetails에 ID를 포함시키고, 서비스 로직을 수정해야 합니다.
        String userEmail = userDetails.getUsername();
        // Long userId = ((CustomUserDetails)userDetails).getId(); 와 같은 방식

        // 여기서는 이메일로 사용자 정보를 찾는 것으로 가정합니다.
        Long bundleId = questionBundleService.createBundleByEmail(requestDto, userEmail);
        return ResponseEntity.ok(bundleId);
    }
}
