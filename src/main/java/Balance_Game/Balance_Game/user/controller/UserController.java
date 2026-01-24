package Balance_Game.Balance_Game.user.controller;

import Balance_Game.Balance_Game.user.dto.UserInfoDto;
import Balance_Game.Balance_Game.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserInfoDto userInfo = userService.getUserInfo(email);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserById(@PathVariable Long userId) {
        UserInfoDto userInfo = userService.getUserInfoById(userId);
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me/nickname")
    public ResponseEntity<UserInfoDto> updateNickname(
            @RequestBody String nickname,
            Authentication authentication) {
        String email = authentication.getName();
        UserInfoDto updatedUser = userService.updateNickname(email, nickname);
        return ResponseEntity.ok(updatedUser);
    }
}