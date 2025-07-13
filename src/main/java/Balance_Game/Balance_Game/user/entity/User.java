package Balance_Game.Balance_Game.user.entity;

import Balance_Game.Balance_Game.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "\"user\"", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_provider_and_id", columnNames = {"provider", "providerId"})
})
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 50)
    private String provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING) // DB에 Enum 이름을 문자열로 저장
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String provider, String providerId, String email, String password, String nickname, Role role) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = (role != null) ? role : Role.USER; // 기본값으로 USER 설정
    }

    public User updateOAuthInfo(String nickname, String provider, String providerId) {
        this.nickname = nickname; // 소셜 프로필의 닉네임으로 업데이트
        this.provider = provider;
        this.providerId = providerId;
        return this;
    }
}
