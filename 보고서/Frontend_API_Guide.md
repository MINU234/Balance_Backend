# 🚀 Balance Game 프론트엔드 개발 가이드

## 📋 개요

이 문서는 Balance Game 백엔드 API와 연동하는 프론트엔드 개발자를 위한 가이드입니다. Spring Boot 3.2.6 기반 REST API와 Next.js 프론트엔드 간의 효율적인 협업을 위한 정보를 제공합니다.

### 🎯 핵심 정보
- **백엔드 URL**: 
  - 개발: `http://localhost:8080`
  - **운영**: `https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net`
- **API 기본 경로**: `/api`
- **API 버전**: v1
- **인증 방식**: JWT (HttpOnly Cookie) + Refresh Token
- **데이터 형식**: JSON
- **페이징**: Spring Data Pageable 방식
- **CORS**: 프론트엔드 도메인 등록 필요
- **SSL**: HTTPS 강제 적용 (운영환경)

---

## 🔐 인증 시스템

### Cookie 기반 JWT 인증
백엔드는 보안을 위해 HttpOnly 쿠키로 JWT 토큰을 관리합니다.

```typescript
// 로그인 API 호출
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net'
  : 'http://localhost:8080';

const login = async (email: string, password: string) => {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // 쿠키 포함 필수!
    body: JSON.stringify({ email, password }),
  });
  
  if (response.ok) {
    // 쿠키가 자동으로 설정됨
    return await response.text(); // "로그인에 성공했습니다."
  }
  throw new Error('로그인 실패');
};

// 로그아웃
const logout = async () => {
  await fetch(`${API_BASE_URL}/api/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
};

// 인증 상태 확인
const getMyInfo = async () => {
  const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
    credentials: 'include',
  });
  
  if (response.ok) {
    return await response.json(); // UserInfoDto
  }
  return null; // 비로그인 상태
};
```

### 인증 컨텍스트 구현 예시
```typescript
// contexts/AuthContext.tsx
import React, { createContext, useContext, useState, useEffect } from 'react';

interface User {
  id: number;
  email: string;
  nickname: string;
  role: 'USER' | 'ADMIN';
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  
  useEffect(() => {
    // 앱 시작시 인증 상태 확인
    checkAuthStatus();
  }, []);
  
  const checkAuthStatus = async () => {
    try {
      const userInfo = await getMyInfo();
      setUser(userInfo);
    } catch {
      setUser(null);
    }
  };
  
  const login = async (email: string, password: string) => {
    await loginAPI(email, password);
    await checkAuthStatus();
  };
  
  const logout = async () => {
    await logoutAPI();
    setUser(null);
  };
  
  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ADMIN'
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
```

---

## 🎮 게임 플레이 플로우

### 1. 비회원 게임 플레이
```typescript
interface GameSession {
  sessionId: number;
  bundleId: number;
  bundleTitle: string;
  tempUserId?: string;
  userEmail?: string;
  questions: Question[];
}

interface Question {
  id: number;
  text: string;
  optionAText: string;
  optionBText: string;
  optionAImageUrl?: string;
  optionBImageUrl?: string;
}

// 1. 게임 시작
const startGame = async (bundleId: number, tempUserId?: string) => {
  const response = await fetch('/api/game/start', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      bundleId,
      tempUserId: tempUserId || `temp-${Date.now()}-${Math.random()}`,
    }),
  });
  
  return await response.json() as GameSession;
};

// 2. 답변 제출
const submitAnswer = async (sessionId: number, questionId: number, option: 'A' | 'B') => {
  await fetch('/api/game/answer', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      sessionId,
      questionId,
      selectedOption: option,
    }),
  });
};

// 3. 게임 완료 및 공유코드 받기
const completeGame = async (sessionId: number) => {
  const response = await fetch(`/api/game/sessions/${sessionId}/complete`, {
    method: 'POST',
    credentials: 'include',
  });
  
  const result = await response.json();
  return result.shareCode; // 8자리 코드
};
```

### 2. 결과 비교 플로우
```typescript
// 공유코드로 세션 정보 조회
const getSessionByShareCode = async (shareCode: string) => {
  const response = await fetch(`/api/game/share/${shareCode}`, {
    credentials: 'include',
  });
  
  if (!response.ok) {
    throw new Error('유효하지 않은 공유코드입니다.');
  }
  
  return await response.json();
};

// 결과 비교
const compareResults = async (shareCode: string, compareSessionId: number) => {
  const response = await fetch(
    `/api/game/compare?shareCode=${shareCode}&compareSessionId=${compareSessionId}`,
    {
      method: 'POST',
      credentials: 'include',
    }
  );
  
  return await response.json() as {
    originalSessionId: number;
    compareSessionId: number;
    bundleTitle: string;
    matchRate: number; // 일치율 (0-100)
    matchCount: number;
    totalQuestions: number;
    comparisons: {
      [questionId: string]: {
        questionId: number;
        originalChoice: 'A' | 'B';
        compareChoice: 'A' | 'B';
        isMatch: boolean;
      };
    };
  };
};
```

---

## 📊 페이징 처리

백엔드는 Spring Data의 Pageable을 사용합니다.

### 페이징 요청 파라미터
```typescript
interface PageRequest {
  page: number;      // 0부터 시작
  size: number;      // 페이지당 항목 수
  sort?: string[];   // 정렬 (예: ["createdDate,desc"])
}

interface PageResponse<T> {
  content: T[];           // 실제 데이터
  pageable: {
    sort: {
      empty: boolean;
    };
    offset: number;
    pageSize: number;
    pageNumber: number;
  };
  totalPages: number;     // 총 페이지 수
  totalElements: number;  // 총 항목 수
  last: boolean;          // 마지막 페이지 여부
  first: boolean;         // 첫 페이지 여부
  number: number;         // 현재 페이지 번호
  size: number;           // 페이지 크기
  numberOfElements: number;
  empty: boolean;
}
```

### 페이징 Hook 구현
```typescript
// hooks/usePagination.ts
import { useState, useEffect } from 'react';

interface UsePaginationProps<T> {
  fetchData: (page: number, size: number) => Promise<PageResponse<T>>;
  initialSize?: number;
}

export const usePagination = <T>({ fetchData, initialSize = 20 }: UsePaginationProps<T>) => {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(initialSize);

  const loadData = async (page: number = currentPage) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetchData(page, pageSize);
      setData(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
      setCurrentPage(page);
    } catch (err) {
      setError(err instanceof Error ? err.message : '데이터 로딩 실패');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(0);
  }, []);

  const goToPage = (page: number) => {
    if (page >= 0 && page < totalPages) {
      loadData(page);
    }
  };

  const nextPage = () => goToPage(currentPage + 1);
  const prevPage = () => goToPage(currentPage - 1);
  const refresh = () => loadData(currentPage);

  return {
    data,
    loading,
    error,
    currentPage,
    totalPages,
    totalElements,
    pageSize,
    goToPage,
    nextPage,
    prevPage,
    refresh,
    hasNext: currentPage < totalPages - 1,
    hasPrev: currentPage > 0,
  };
};
```

---

## 🔍 질문 및 묶음 관리

### 인기 질문/묶음 조회
```typescript
// 인기 질문 조회
const getPopularQuestions = async (page: number = 0, size: number = 20) => {
  const response = await fetch(
    `/api/questions/popular?page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  return await response.json() as PageResponse<PopularQuestionDto>;
};

// 인기 묶음 조회
const getPopularBundles = async (page: number = 0, size: number = 20) => {
  const response = await fetch(
    `/api/question-bundles/popular?page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  return await response.json() as PageResponse<PopularBundleDto>;
};

// 묶음 검색
const searchBundles = async (query: string, page: number = 0, size: number = 20) => {
  const response = await fetch(
    `/api/question-bundles/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  return await response.json() as PageResponse<PopularBundleDto>;
};
```

### 회원 전용 - 질문/묶음 생성
```typescript
// 질문 생성 (승인 필요)
const createQuestion = async (question: {
  text: string;
  optionAText: string;
  optionBText: string;
  keyword: string;
  optionAImageUrl?: string;
  optionBImageUrl?: string;
}) => {
  const response = await fetch('/api/questions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(question),
  });
  
  if (!response.ok) {
    throw new Error('질문 생성 실패');
  }
  
  return await response.json(); // questionId
};

// 질문 묶음 생성
const createBundle = async (bundle: {
  title: string;
  description: string;
  isPublic: boolean;
  questionIds: number[];
}) => {
  const response = await fetch('/api/question-bundles', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(bundle),
  });
  
  if (!response.ok) {
    throw new Error('묶음 생성 실패');
  }
  
  return await response.json(); // bundleId
};
```

---

## 📊 마이페이지 API

### 내 정보 및 통계
```typescript
interface MyPageStats {
  totalQuestions: number;
  approvedQuestions: number;
  pendingQuestions: number;
  rejectedQuestions: number;
  totalBundles: number;
  totalGamesPlayed: number;
}

// 내 통계 조회
const getMyStats = async (): Promise<MyPageStats> => {
  const response = await fetch('/api/my/stats', {
    credentials: 'include',
  });
  
  if (!response.ok) {
    throw new Error('통계 조회 실패');
  }
  
  return await response.json();
};

// 내 질문 조회 (모든 상태)
const getMyQuestions = async (page: number = 0, size: number = 10) => {
  const response = await fetch(
    `/api/my/questions?page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  return await response.json() as PageResponse<QuestionDto>;
};

// 상태별 질문 조회
const getMyQuestionsByStatus = async (
  status: 'PENDING' | 'APPROVED' | 'REJECTED',
  page: number = 0,
  size: number = 10
) => {
  const response = await fetch(
    `/api/my/questions/status/${status}?page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  return await response.json() as PageResponse<QuestionDto>;
};

// 내 게임 기록
const getMyGameHistory = async (page: number = 0, size: number = 10) => {
  const response = await fetch(
    `/api/my/game-history?page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  return await response.json() as PageResponse<GameResultDto>;
};
```

---

## 🛡️ 관리자 기능 (ADMIN만 접근 가능)

### 질문 승인 관리
```typescript
// 승인 대기 질문 조회
const getPendingQuestions = async (page: number = 0, size: number = 20) => {
  const response = await fetch(
    `/api/admin/questions/pending?page=${page}&size=${size}`,
    { credentials: 'include' }
  );
  
  if (response.status === 403) {
    throw new Error('관리자 권한이 필요합니다.');
  }
  
  return await response.json() as PageResponse<QuestionDto>;
};

// 질문 승인
const approveQuestion = async (questionId: number) => {
  const response = await fetch(`/api/admin/questions/${questionId}/approve`, {
    method: 'POST',
    credentials: 'include',
  });
  
  if (!response.ok) {
    throw new Error('승인 처리 실패');
  }
};

// 질문 거절
const rejectQuestion = async (questionId: number, reason: string) => {
  const response = await fetch(`/api/admin/questions/${questionId}/reject`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ reason }),
  });
  
  if (!response.ok) {
    throw new Error('거절 처리 실패');
  }
};

// 일괄 승인
const bulkApproveQuestions = async (questionIds: number[]) => {
  const response = await fetch('/api/admin/questions/bulk-approve', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ questionIds }),
  });
  
  if (!response.ok) {
    throw new Error('일괄 승인 실패');
  }
};
```

---

## 🎨 UI 컴포넌트 예시

### 게임 플레이 컴포넌트
```tsx
// components/GamePlay.tsx
import React, { useState, useEffect } from 'react';

interface GamePlayProps {
  bundleId: number;
  onGameComplete: (shareCode: string) => void;
}

export const GamePlay: React.FC<GamePlayProps> = ({ bundleId, onGameComplete }) => {
  const [session, setSession] = useState<GameSession | null>(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, 'A' | 'B'>>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    initializeGame();
  }, [bundleId]);

  const initializeGame = async () => {
    setLoading(true);
    try {
      const tempUserId = `temp-${Date.now()}-${Math.random()}`;
      const gameSession = await startGame(bundleId, tempUserId);
      setSession(gameSession);
    } catch (error) {
      console.error('게임 시작 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAnswer = async (option: 'A' | 'B') => {
    if (!session) return;

    const currentQuestion = session.questions[currentQuestionIndex];
    
    try {
      await submitAnswer(session.sessionId, currentQuestion.id, option);
      
      setAnswers(prev => ({
        ...prev,
        [currentQuestion.id]: option,
      }));

      if (currentQuestionIndex < session.questions.length - 1) {
        setCurrentQuestionIndex(prev => prev + 1);
      } else {
        // 게임 완료
        const shareCode = await completeGame(session.sessionId);
        onGameComplete(shareCode);
      }
    } catch (error) {
      console.error('답변 제출 실패:', error);
    }
  };

  if (loading || !session) {
    return <div>게임을 준비하고 있습니다...</div>;
  }

  const currentQuestion = session.questions[currentQuestionIndex];
  const progress = ((currentQuestionIndex + 1) / session.questions.length) * 100;

  return (
    <div className="game-container">
      <div className="progress-bar">
        <div className="progress" style={{ width: `${progress}%` }} />
      </div>
      
      <div className="question-counter">
        {currentQuestionIndex + 1} / {session.questions.length}
      </div>
      
      <h2 className="question-text">{currentQuestion.text}</h2>
      
      <div className="options">
        <button 
          className="option-button option-a"
          onClick={() => handleAnswer('A')}
        >
          {currentQuestion.optionAImageUrl && (
            <img src={currentQuestion.optionAImageUrl} alt="Option A" />
          )}
          <span>{currentQuestion.optionAText}</span>
        </button>
        
        <button 
          className="option-button option-b"
          onClick={() => handleAnswer('B')}
        >
          {currentQuestion.optionBImageUrl && (
            <img src={currentQuestion.optionBImageUrl} alt="Option B" />
          )}
          <span>{currentQuestion.optionBText}</span>
        </button>
      </div>
    </div>
  );
};
```

### 결과 비교 컴포넌트
```tsx
// components/ResultComparison.tsx
import React, { useState, useEffect } from 'react';

interface ResultComparisonProps {
  shareCode: string;
  compareSessionId: number;
}

export const ResultComparison: React.FC<ResultComparisonProps> = ({ 
  shareCode, 
  compareSessionId 
}) => {
  const [comparison, setComparison] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadComparison();
  }, [shareCode, compareSessionId]);

  const loadComparison = async () => {
    try {
      const result = await compareResults(shareCode, compareSessionId);
      setComparison(result);
    } catch (error) {
      console.error('비교 결과 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div>결과를 비교하고 있습니다...</div>;
  }

  if (!comparison) {
    return <div>비교 결과를 불러올 수 없습니다.</div>;
  }

  return (
    <div className="comparison-container">
      <h2>{comparison.bundleTitle}</h2>
      
      <div className="match-summary">
        <div className="match-rate">
          <span className="rate">{comparison.matchRate.toFixed(1)}%</span>
          <span className="label">일치율</span>
        </div>
        <div className="match-count">
          {comparison.matchCount} / {comparison.totalQuestions} 문항 일치
        </div>
      </div>
      
      <div className="detailed-comparison">
        {Object.values(comparison.comparisons).map((item: any) => (
          <div 
            key={item.questionId} 
            className={`comparison-item ${item.isMatch ? 'match' : 'different'}`}
          >
            <div className="choices">
              <span className="original-choice">You: {item.originalChoice}</span>
              <span className="compare-choice">Friend: {item.compareChoice}</span>
            </div>
            <div className="match-indicator">
              {item.isMatch ? '✅' : '❌'}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## 🚨 에러 처리

### 표준 에러 응답 형식
```typescript
interface ErrorResponse {
  message: string;
  timestamp: string;
  status: number;
  error: string;
  path: string;
}

// 에러 처리 유틸리티
const handleApiError = (error: any): string => {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  
  switch (error.response?.status) {
    case 401:
      return '로그인이 필요합니다.';
    case 403:
      return '권한이 없습니다.';
    case 404:
      return '요청한 리소스를 찾을 수 없습니다.';
    case 500:
      return '서버 오류가 발생했습니다.';
    default:
      return '알 수 없는 오류가 발생했습니다.';
  }
};

// API 호출 래퍼
const apiRequest = async <T>(
  url: string, 
  options: RequestInit = {}
): Promise<T> => {
  const response = await fetch(url, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP ${response.status}`);
  }

  return await response.json();
};
```

---

## 🔧 개발 환경 설정

### Next.js 프록시 설정 (개발환경)
```javascript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
```

### 환경 변수
```bash
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

### CORS 정책 확인
백엔드에서 다음과 같이 CORS가 설정되어 있습니다:
```java
@CrossOrigin(origins = "*") // 개발용 - 운영에서는 특정 도메인으로 제한
```

---

## 📱 모바일 대응

### 반응형 디자인 고려사항
- **게임 플레이**: 터치 친화적인 큰 버튼
- **이미지 최적화**: 다양한 해상도 지원
- **페이징**: 무한 스크롤 vs 페이지네이션 선택
- **공유 기능**: 네이티브 Web Share API 활용

```typescript
// 네이티브 공유 기능
const shareResult = async (shareCode: string) => {
  const shareData = {
    title: 'Balance Game 결과',
    text: '나와 얼마나 비슷한지 확인해보세요!',
    url: `${window.location.origin}/compare/${shareCode}`,
  };

  if (navigator.share) {
    await navigator.share(shareData);
  } else {
    // 폴백: 클립보드 복사
    await navigator.clipboard.writeText(shareData.url);
    alert('링크가 복사되었습니다!');
  }
};
```

---

## 🎯 주요 사용자 플로우

### 1. 비회원 게임 플레이
```
1. 홈페이지 접속
2. 인기 묶음 조회 (`GET /api/question-bundles/popular`)
3. 묶음 선택 및 게임 시작 (`POST /api/game/start`)
4. 질문별 답변 제출 (`POST /api/game/answer`)
5. 게임 완료 및 공유코드 받기 (`POST /api/game/sessions/{id}/complete`)
6. 결과 공유 (공유코드 전달)
```

### 2. 결과 비교
```
1. 공유코드 입력 또는 링크 접속
2. 공유코드 검증 (`GET /api/game/share/{shareCode}/validate`)
3. 원본 게임과 같은 묶음으로 게임 시작
4. 게임 완료 후 결과 비교 (`POST /api/game/compare`)
5. 일치율 및 문항별 비교 결과 표시
```

### 3. 회원 질문 생성
```
1. 회원가입/로그인 (`POST /api/auth/signup`, `POST /api/auth/login`)
2. 질문 생성 (`POST /api/questions`) - PENDING 상태
3. 마이페이지에서 승인 상태 확인 (`GET /api/my/questions/status/PENDING`)
4. 관리자 승인 후 질문 활성화
```

---

## 📞 백엔드 개발자 연락처

**API 관련 문의사항**:
- 이메일: [백엔드 개발자 이메일]
- 슬랙: #balance-game-dev
- API 문서: `API_Documentation.md` 참조

**주요 확인사항**:
- 새로운 API 추가 시 문서 업데이트 요청
- 에러 응답 형식 변경 시 사전 공지
- 페이징 파라미터 변경 시 호환성 확인

---

*이 가이드는 지속적으로 업데이트됩니다. 새로운 API나 변경사항이 있을 때마다 문서를 확인해 주세요.*