# 🌐 Balance Game API 실제 엔드포인트 가이드

## 📍 서비스 정보
- **운영 서버**: https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net
- **개발 서버**: http://localhost:8080
- **API 기본 경로**: `/api`
- **상태**: 정상 운영 중 ✅
- **마지막 업데이트**: 2025년 1월 14일

## 🔧 환경별 설정

### JavaScript/TypeScript 환경 변수
```typescript
// .env.local 또는 .env.production
const config = {
  development: {
    API_BASE_URL: 'http://localhost:8080',
    FRONTEND_URL: 'http://localhost:3000'
  },
  production: {
    API_BASE_URL: 'https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net',
    FRONTEND_URL: 'https://your-frontend-domain.com'
  }
};

const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? config.production.API_BASE_URL
  : config.development.API_BASE_URL;
```

### Axios 기본 설정
```typescript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // 쿠키 포함 (JWT 인증)
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// 요청 인터셉터 (CSRF, 인증 처리)
apiClient.interceptors.request.use((config) => {
  // CORS preflight 처리를 위한 헤더
  if (config.method !== 'get') {
    config.headers['X-Requested-With'] = 'XMLHttpRequest';
  }
  return config;
});

// 응답 인터셉터 (에러 처리, 토큰 갱신)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 토큰 만료 시 로그인 페이지로 리다이렉트
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## 🔐 인증 API

### 1. 로그인
```http
POST /api/auth/login
Content-Type: application/json
```

**요청 Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": "로그인에 성공했습니다.",
  "message": "SUCCESS",
  "timestamp": "2025-01-14T15:30:00"
}
```

**JavaScript 예제**:
```typescript
const login = async (email: string, password: string) => {
  try {
    const response = await apiClient.post('/api/auth/login', {
      email,
      password
    });
    
    console.log('로그인 성공:', response.data);
    // JWT 토큰은 HttpOnly 쿠키로 자동 설정됨
    return response.data;
  } catch (error) {
    console.error('로그인 실패:', error.response?.data);
    throw error;
  }
};
```

### 2. OAuth2 소셜 로그인
```http
GET /api/oauth2/authorization/google
GET /api/oauth2/authorization/kakao
```

**Google 로그인 버튼**:
```typescript
const handleGoogleLogin = () => {
  window.location.href = `${API_BASE_URL}/api/oauth2/authorization/google`;
};

const handleKakaoLogin = () => {
  window.location.href = `${API_BASE_URL}/api/oauth2/authorization/kakao`;
};
```

### 3. 로그아웃
```http
POST /api/auth/logout
```

**JavaScript 예제**:
```typescript
const logout = async () => {
  try {
    await apiClient.post('/api/auth/logout');
    console.log('로그아웃 성공');
    // 필요시 로컬 상태 초기화
    window.location.href = '/';
  } catch (error) {
    console.error('로그아웃 실패:', error);
  }
};
```

### 4. 현재 사용자 정보
```http
GET /api/auth/me
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "사용자닉네임",
    "role": "USER",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

## 🎮 게임 플레이 API

### 1. 게임 시작
```http
POST /api/game/start
Content-Type: application/json
```

**요청 Body**:
```json
{
  "bundleId": 1,
  "userEmail": "user@example.com" // nullable (비회원은 null)
}
```

**성공 응답 (201)**:
```json
{
  "success": true,
  "data": {
    "sessionId": 12345,
    "bundleId": 1,
    "bundleTitle": "연애 vs 우정",
    "totalQuestions": 10,
    "currentQuestionIndex": 0,
    "currentQuestion": {
      "id": 101,
      "text": "소중한 사람과의 시간",
      "optionAText": "연인과의 데이트",
      "optionBText": "친구들과의 모임",
      "optionAImageUrl": "https://...",
      "optionBImageUrl": "https://..."
    }
  }
}
```

**JavaScript 예제**:
```typescript
const startGame = async (bundleId: number, userEmail?: string) => {
  try {
    const response = await apiClient.post('/api/game/start', {
      bundleId,
      userEmail: userEmail || null // 비회원은 null
    });
    
    console.log('게임 시작:', response.data);
    return response.data.data;
  } catch (error) {
    console.error('게임 시작 실패:', error.response?.data);
    throw error;
  }
};
```

### 2. 답변 제출
```http
POST /api/game/answer
Content-Type: application/json
```

**요청 Body**:
```json
{
  "sessionId": 12345,
  "questionId": 101,
  "selectedOption": "A" // "A" or "B"
}
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "sessionId": 12345,
    "currentQuestionIndex": 1,
    "nextQuestion": {
      "id": 102,
      "text": "휴가 계획",
      "optionAText": "연인과 여행",
      "optionBText": "친구들과 여행"
    },
    "isGameComplete": false
  }
}
```

### 3. 게임 완료
```http
POST /api/game/sessions/{sessionId}/complete
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "sessionId": 12345,
    "shareCode": "A1B2C3D4", // 8자리 공유코드
    "expiresAt": "2025-01-21T15:30:00",
    "results": {
      "totalQuestions": 10,
      "answers": [
        {
          "questionId": 101,
          "questionText": "소중한 사람과의 시간",
          "selectedOption": "A",
          "selectedText": "연인과의 데이트"
        }
      ]
    }
  }
}
```

### 4. 공유코드로 결과 비교
```http
POST /api/game/compare?shareCode=A1B2C3D4&compareSessionId=12346
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "shareCode": "A1B2C3D4",
    "originalSession": {
      "sessionId": 12345,
      "completedAt": "2025-01-14T15:00:00"
    },
    "compareSession": {
      "sessionId": 12346,
      "completedAt": "2025-01-14T15:30:00"
    },
    "comparison": {
      "matchPercentage": 80.0,
      "totalQuestions": 10,
      "matchedAnswers": 8,
      "questionComparisons": [
        {
          "questionId": 101,
          "questionText": "소중한 사람과의 시간",
          "originalAnswer": "A",
          "compareAnswer": "A",
          "isMatch": true
        }
      ]
    }
  }
}
```

---

## 📚 질문 및 묶음 API

### 1. 인기 질문 조회
```http
GET /api/questions/popular?page=0&size=20
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "text": "소중한 사람과의 시간",
        "optionAText": "연인과의 데이트",
        "optionBText": "친구들과의 모임",
        "keyword": "연애",
        "createdBy": {
          "id": 1,
          "nickname": "질문왕"
        },
        "statistics": {
          "optionACount": 150,
          "optionBCount": 120,
          "totalCount": 270
        }
      }
    ],
    "pageable": {
      "page": 0,
      "size": 20,
      "sort": "playCount,DESC"
    },
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### 2. 인기 묶음 조회
```http
GET /api/question-bundles/popular?page=0&size=12
```

### 3. 묶음 검색
```http
GET /api/question-bundles/search?query=연애&page=0&size=12
```

### 4. 묶음 상세 조회
```http
GET /api/question-bundles/{bundleId}
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "연애 vs 우정",
    "description": "연애와 우정 사이에서의 선택들",
    "isPublic": true,
    "questionCount": 10,
    "playCount": 1250,
    "createdBy": {
      "id": 1,
      "nickname": "게임마스터"
    },
    "questions": [
      {
        "id": 101,
        "text": "소중한 사람과의 시간",
        "optionAText": "연인과의 데이트",
        "optionBText": "친구들과의 모임"
      }
    ],
    "createdAt": "2024-12-01T10:00:00"
  }
}
```

---

## 👤 마이페이지 API (인증 필요)

### 1. 내 게임 기록
```http
GET /api/my/game-history?page=0&size=10
```

### 2. 내 질문 목록
```http
GET /api/my/questions?status=APPROVED&page=0&size=10
```

### 3. 내 묶음 목록
```http
GET /api/my/question-bundles?page=0&size=10
```

### 4. 내 통계
```http
GET /api/my/stats
```

**성공 응답 (200)**:
```json
{
  "success": true,
  "data": {
    "totalGamesPlayed": 25,
    "totalQuestionsCreated": 15,
    "totalBundlesCreated": 5,
    "approvedQuestions": 12,
    "pendingQuestions": 2,
    "rejectedQuestions": 1,
    "mostPlayedKeyword": "연애",
    "averageMatchPercentage": 75.5
  }
}
```

---

## ⚙️ 관리자 API (ADMIN 권한 필요)

### 1. 대시보드 통계
```http
GET /api/admin/dashboard
```

### 2. 승인 대기 질문 목록
```http
GET /api/admin/questions/pending?page=0&size=20
```

### 3. 질문 승인
```http
POST /api/admin/questions/{questionId}/approve
```

### 4. 질문 거절
```http
POST /api/admin/questions/{questionId}/reject
Content-Type: application/json
```

**요청 Body**:
```json
{
  "reason": "부적절한 내용이 포함되어 있습니다."
}
```

---

## 🔧 유틸리티 및 헬스체크

### 1. 애플리케이션 상태 확인
```http
GET /actuator/health
```

**성공 응답 (200)**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 2. 키워드 통계
```http
GET /api/keywords/stats
```

### 3. 서버 시간
```http
GET /api/server-time
```

---

## 🚨 에러 응답 형식

### 표준 에러 응답
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력값이 올바르지 않습니다.",
    "details": [
      {
        "field": "email",
        "message": "이메일 형식이 올바르지 않습니다."
      }
    ]
  },
  "timestamp": "2025-01-14T15:30:00"
}
```

### 주요 에러 코드
| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | `VALIDATION_ERROR` | 입력값 검증 실패 |
| 401 | `UNAUTHORIZED` | 인증 필요 |
| 403 | `FORBIDDEN` | 권한 없음 |
| 404 | `NOT_FOUND` | 리소스를 찾을 수 없음 |
| 409 | `CONFLICT` | 중복된 리소스 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

### JavaScript 에러 처리
```typescript
const handleApiCall = async () => {
  try {
    const response = await apiClient.get('/api/some-endpoint');
    return response.data;
  } catch (error) {
    if (error.response) {
      // 서버가 응답했지만 에러 상태
      const { status, data } = error.response;
      console.error(`API 에러 (${status}):`, data.error);
      
      switch (status) {
        case 401:
          // 로그인 페이지로 리다이렉트
          window.location.href = '/login';
          break;
        case 403:
          alert('접근 권한이 없습니다.');
          break;
        case 404:
          alert('요청한 데이터를 찾을 수 없습니다.');
          break;
        default:
          alert(data.error.message || '오류가 발생했습니다.');
      }
    } else if (error.request) {
      // 네트워크 오류
      console.error('네트워크 오류:', error.request);
      alert('네트워크 연결을 확인해주세요.');
    } else {
      console.error('알 수 없는 오류:', error.message);
      alert('알 수 없는 오류가 발생했습니다.');
    }
  }
};
```

---

## 🌐 CORS 및 보안 설정

### 현재 허용된 헤더
```
Content-Type
Authorization  
X-Requested-With
Cache-Control
```

### 프론트엔드 도메인 등록 방법
현재 CORS 설정에 프론트엔드 도메인을 추가하려면 백엔드 환경 변수를 업데이트해야 합니다:

```bash
# Azure App Service 환경 변수 설정
az webapp config appsettings set \
  --name balance-game \
  --resource-group MINWOO \
  --settings \
    CORS_ALLOWED_ORIGINS="https://your-frontend-domain.com,https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net"
```

### 개발환경 프록시 설정 (Next.js)
```javascript
// next.config.js
module.exports = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*'
      }
    ];
  }
};
```

---

## 📝 실제 사용 예제

### React Hook 예제
```typescript
// hooks/useAuth.ts
import { useState, useEffect } from 'react';

export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const response = await apiClient.get('/api/auth/me');
      setUser(response.data.data);
    } catch (error) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    const response = await apiClient.post('/api/auth/login', {
      email,
      password
    });
    await checkAuthStatus();
    return response.data;
  };

  const logout = async () => {
    await apiClient.post('/api/auth/logout');
    setUser(null);
  };

  return {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user
  };
};
```

### 게임 플레이 컴포넌트 예제
```typescript
// components/GamePlay.tsx
import { useState, useEffect } from 'react';

interface GamePlayProps {
  bundleId: number;
}

export const GamePlay: React.FC<GamePlayProps> = ({ bundleId }) => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  const startGame = async () => {
    try {
      const response = await apiClient.post('/api/game/start', {
        bundleId,
        userEmail: null // 비회원으로 플레이
      });
      setSession(response.data.data);
    } catch (error) {
      console.error('게임 시작 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const submitAnswer = async (selectedOption: 'A' | 'B') => {
    try {
      const response = await apiClient.post('/api/game/answer', {
        sessionId: session.sessionId,
        questionId: session.currentQuestion.id,
        selectedOption
      });

      if (response.data.data.isGameComplete) {
        // 게임 완료 처리
        completeGame();
      } else {
        // 다음 질문으로 업데이트
        setSession(prev => ({
          ...prev,
          ...response.data.data
        }));
      }
    } catch (error) {
      console.error('답변 제출 실패:', error);
    }
  };

  const completeGame = async () => {
    try {
      const response = await apiClient.post(
        `/api/game/sessions/${session.sessionId}/complete`
      );
      
      const { shareCode } = response.data.data;
      alert(`게임 완료! 공유코드: ${shareCode}`);
      
      // 결과 페이지로 이동 또는 결과 표시
    } catch (error) {
      console.error('게임 완료 실패:', error);
    }
  };

  useEffect(() => {
    startGame();
  }, [bundleId]);

  if (loading) return <div>게임을 준비하고 있습니다...</div>;
  if (!session) return <div>게임을 시작할 수 없습니다.</div>;

  return (
    <div>
      <h2>{session.bundleTitle}</h2>
      <p>질문 {session.currentQuestionIndex + 1} / {session.totalQuestions}</p>
      
      <div>
        <h3>{session.currentQuestion.text}</h3>
        <button onClick={() => submitAnswer('A')}>
          {session.currentQuestion.optionAText}
        </button>
        <button onClick={() => submitAnswer('B')}>
          {session.currentQuestion.optionBText}
        </button>
      </div>
    </div>
  );
};
```

---

## 📞 지원 및 문의

### 기술 지원
- **API 문의**: 백엔드 개발팀
- **CORS 설정 요청**: DevOps 팀
- **버그 리포트**: GitHub Issues

### 추가 리소스
- [API 상세 문서](./API_Documentation.md)
- [프론트엔드 개발 가이드](./Frontend_Development_Guide.md)
- [에러 코드 전체 목록](./Error_Codes.md)

---

*마지막 업데이트: 2025년 1월 14일*  
*실제 운영 서버 기준으로 작성된 문서입니다.*