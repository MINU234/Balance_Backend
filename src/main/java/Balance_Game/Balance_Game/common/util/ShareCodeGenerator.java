package Balance_Game.Balance_Game.common.util;

import Balance_Game.Balance_Game.common.constants.GameConstants;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ShareCodeGenerator {
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * 8자리 공유 코드 생성 (개선된 알고리즘)
     * 타임스탬프 기반 접두사 + 랜덤 문자열로 충돌 가능성 최소화
     */
    public String generateShareCode() {
        // 현재 시간의 밀리초를 36진법으로 변환하여 3자리 접두사 생성
        long timestamp = System.currentTimeMillis();
        String timePrefix = Long.toString(timestamp % 46656, 36).toUpperCase(); // 36^3 = 46656
        
        // 부족한 자리는 0으로 패딩
        timePrefix = String.format("%3s", timePrefix).replace(' ', '0');
        
        // 나머지 5자리는 랜덤 생성
        StringBuilder randomSuffix = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            randomSuffix.append(GameConstants.SHARE_CODE_CHARACTERS.charAt(
                random.nextInt(GameConstants.SHARE_CODE_CHARACTERS.length())));
        }
        
        return timePrefix + randomSuffix.toString();
    }
    
    /**
     * 백업용 순수 랜덤 코드 생성
     * 타임스탬프 기반 생성이 실패할 경우 사용
     */
    public String generateRandomShareCode() {
        StringBuilder code = new StringBuilder(GameConstants.SHARE_CODE_LENGTH);
        for (int i = 0; i < GameConstants.SHARE_CODE_LENGTH; i++) {
            code.append(GameConstants.SHARE_CODE_CHARACTERS.charAt(
                random.nextInt(GameConstants.SHARE_CODE_CHARACTERS.length())));
        }
        return code.toString();
    }
    
    /**
     * 코드 형식 검증
     */
    public boolean isValidShareCode(String code) {
        if (code == null || code.length() != GameConstants.SHARE_CODE_LENGTH) {
            return false;
        }
        return code.matches("^[A-Z0-9]{" + GameConstants.SHARE_CODE_LENGTH + "}$");
    }
    
    /**
     * 공유코드에서 생성 시간 추출 (디버깅/분석용)
     */
    public LocalDateTime extractTimestamp(String shareCode) {
        if (!isValidShareCode(shareCode)) {
            return null;
        }
        
        try {
            String timePrefix = shareCode.substring(0, 3);
            long timeValue = Long.parseLong(timePrefix, 36);
            
            // 현재 시간 기준으로 가장 가까운 시간 계산
            long currentTime = System.currentTimeMillis();
            long baseTime = (currentTime / 46656) * 46656;
            long actualTime = baseTime + timeValue;
            
            // 미래 시간이면 이전 주기로 조정
            if (actualTime > currentTime) {
                actualTime -= 46656;
            }
            
            return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(actualTime), 
                java.time.ZoneId.systemDefault()
            );
        } catch (Exception e) {
            return null;
        }
    }
}
