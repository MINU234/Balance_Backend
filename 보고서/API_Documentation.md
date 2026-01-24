# Balance Game API Documentation

## 📌 주요 변경사항

### 1. 질문 승인 시스템
- 모든 질문은 생성 시 `PENDING` 상태로 생성
- 관리자가 승인해야 일반 사용자에게 노출
- 거절 시 사유 제공

### 2. 8자리 공유 코드 시스템
- 게임 완료 시 8자리 공유 코드 자동 생성
- 공유 코드로 다른 사람과 결과 비교 가능
- 개인 플레이 중심 (player1, player2 제거)

### 3. 권한 체계
- 비회원: 게임 플레이, 조회만 가능
- 일반 회원: 질문/묶음 생성 요청 가능
- 관리자: 질문 승인/거절 권한

---

## 🎮 게임 플레이 API

### 게임 시작
```http
POST /api/game/start
Content-Type: application/json

# 회원
{
  "bundleId": 1,
  "userEmail": "user@example.com"
}

# 비회원
{
  "bundleId": 1,
  "tempUserId": "temp-123"
}

# 공유 코드로 시작
{
  "bundleId": 1,
  "shareCode": "ABC12345"
}
```

### 답변 제출
```http
POST /api/game/answer
Content-Type: application/json

{
  "sessionId": 1,
  "questionId": 1,
  "selectedOption": "A"
}
```

### 게임 완료 및 공유 코드 받기
```http
POST /api/game/sessions/{sessionId}/complete

Response:
{
  "shareCode": "ABC12345"
}
```

### 결과 조회
```http
GET /api/game/sessions/{sessionId}/results

Response:
{
  "sessionId": 1,
  "bundleTitle": "연애 밸런스 게임",
  "shareCode": "ABC12345",
  "userChoices": {
    "1": "A",
    "2": "B"
  },
  "totalQuestions": 10
}
```

### 공유 코드로 세션 조회
```http
GET /api/game/share/{shareCode}

Response:
{
  "sessionId": 1,
  "bundleId": 1,
  "bundleTitle": "연애 밸런스 게임",
  "shareCode": "ABC12345"
}
```

### 결과 비교
```http
POST /api/game/compare?shareCode=ABC12345&compareSessionId=2

Response:
{
  "originalSessionId": 1,
  "compareSessionId": 2,
  "bundleTitle": "연애 밸런스 게임",
  "matchRate": 70.0,
  "matchCount": 7,
  "totalQuestions": 10,
  "comparisons": {
    "1": {
      "questionId": 1,
      "originalChoice": "A",
      "compareChoice": "A",
      "isMatch": true
    }
  }
}
```

---

## 👤 질문 관리 API

### 질문 생성 (회원 전용)
```http
POST /api/questions
Content-Type: application/json
Authorization: Bearer {token}

{
  "text": "아침형 인간 vs 저녁형 인간",
  "optionAText": "아침형 인간",
  "optionBText": "저녁형 인간",
  "keyword": "라이프스타일",
  "optionAImageUrl": "https://...",
  "optionBImageUrl": "https://..."
}
```

### 인기 질문 조회 (승인된 것만)
```http
GET /api/questions/popular?page=0&size=20
```

---

## 🔐 관리자 API

### 승인 대기 질문 조회
```http
GET /api/admin/questions/pending?page=0&size=20
Authorization: Bearer {admin_token}
```

### 질문 승인
```http
POST /api/admin/questions/{questionId}/approve
Authorization: Bearer {admin_token}
```

### 질문 거절
```http
POST /api/admin/questions/{questionId}/reject
Content-Type: application/json
Authorization: Bearer {admin_token}

{
  "reason": "부적절한 내용이 포함되어 있습니다."
}
```

### 일괄 승인
```http
POST /api/admin/questions/bulk-approve
Content-Type: application/json
Authorization: Bearer {admin_token}

{
  "questionIds": [1, 2, 3, 4, 5]
}
```

### 승인/거절 이력 조회
```http
GET /api/admin/questions/history?status=APPROVED&page=0&size=20
Authorization: Bearer {admin_token}
```

---

## 📊 마이페이지 API

### 내 질문 묶음 조회
```http
GET /api/my/question-bundles?page=0&size=10
Authorization: Bearer {token}
```

### 내 질문 조회 (모든 상태)
```http
GET /api/my/questions?page=0&size=10
Authorization: Bearer {token}
```

### 내 질문 승인 상태별 조회
```http
GET /api/my/questions/status/PENDING?page=0&size=10
Authorization: Bearer {token}

# 가능한 상태: PENDING, APPROVED, REJECTED
```

### 내 게임 기록 조회
```http
GET /api/my/game-history?page=0&size=10
Authorization: Bearer {token}
```

### 내 통계 조회
```http
GET /api/my/stats
Authorization: Bearer {token}

Response:
{
  "totalQuestions": 50,
  "approvedQuestions": 30,
  "pendingQuestions": 15,
  "rejectedQuestions": 5,
  "totalBundles": 10,
  "totalGamesPlayed": 100
}
```

---

## 🔑 인증 API

### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG..."
}
```

### 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자닉네임"
}
```

---

## 📝 응답 코드

- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 필요
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스를 찾을 수 없음
- `500 Internal Server Error`: 서버 오류

---

## 🎯 테스트 시나리오

### 시나리오 1: 비회원 게임 플레이
1. 질문 묶음 목록 조회
2. 게임 시작 (tempUserId 생성)
3. 질문에 답변
4. 게임 완료 및 공유 코드 받기
5. 친구에게 공유 코드 전달

### 시나리오 2: 공유 코드로 비교
1. 공유 코드로 세션 정보 조회
2. 같은 묶음으로 게임 시작
3. 질문에 답변
4. 게임 완료
5. 원본과 비교

### 시나리오 3: 질문 생성 및 승인
1. (회원) 질문 생성 요청
2. (회원) 마이페이지에서 PENDING 상태 확인
3. (관리자) 승인 대기 목록 조회
4. (관리자) 질문 승인 또는 거절
5. (회원) 승인/거절 결과 확인

---

## 🚀 배포 체크리스트

- [ ] 데이터베이스 마이그레이션 스크립트 실행
- [ ] 기존 질문 승인 상태 업데이트 (APPROVED로)
- [ ] 관리자 계정 생성 및 ROLE 설정
- [ ] 공유 코드 인덱스 생성 확인
- [ ] 캐시 설정 확인
- [ ] CORS 설정 확인
