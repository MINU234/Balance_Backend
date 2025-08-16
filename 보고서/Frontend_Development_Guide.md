# 🎨 Balance Game 프론트엔드 개발 가이드

## 🌐 **실제 서비스 정보**
- **백엔드 API**: https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net
- **API 경로**: `/api`
- **서비스 상태**: 정상 운영 중 ✅
- **마지막 업데이트**: 2025년 1월 14일

## 📋 프로젝트 개요

이 문서는 **실제 운영 중인** Balance Game 백엔드 API와 연동하는 프론트엔드 개발을 위한 종합 가이드입니다. Next.js 기반으로 Spring Boot 백엔드와 연동하는 현대적인 웹 애플리케이션 개발 방법을 제시합니다.

### 🎯 목표
- **개인 중심 밸런스 게임** 플랫폼 구현
- **8자리 공유코드** 기반 결과 비교 시스템
- **승인제 콘텐츠 관리** 시스템 UI
- **반응형 디자인** 및 모바일 최적화

---

## 🛠️ 기술 스택 권장사항

### 필수 기술
```json
{
  "frontend": {
    "framework": "Next.js 14+ (App Router)",
    "language": "TypeScript",
    "styling": "Tailwind CSS + shadcn/ui",
    "state": "Zustand 또는 Context API",
    "forms": "React Hook Form + Zod",
    "http": "Fetch API (native)"
  },
  "development": {
    "package_manager": "pnpm",
    "linting": "ESLint + Prettier",
    "testing": "Jest + React Testing Library",
    "icons": "Lucide React"
  }
}
```

### 추천 라이브러리
```bash
# 핵심 의존성
npm install next@latest react@latest react-dom@latest typescript
npm install @types/node @types/react @types/react-dom

# UI 및 스타일링
npm install tailwindcss@latest autoprefixer postcss
npm install @radix-ui/react-dialog @radix-ui/react-dropdown-menu
npm install lucide-react class-variance-authority clsx tailwind-merge

# 폼 및 검증
npm install react-hook-form @hookform/resolvers zod

# 상태 관리
npm install zustand

# 유틸리티
npm install date-fns
npm install react-hot-toast  # 알림
npm install framer-motion    # 애니메이션 (선택)
```

---

## 📁 프로젝트 구조

```
balance-game-frontend/
├── app/                          # Next.js App Router
│   ├── (auth)/                   # 인증 관련 라우트 그룹
│   │   ├── login/
│   │   └── signup/
│   ├── admin/                    # 관리자 전용 페이지
│   │   ├── dashboard/
│   │   ├── questions/
│   │   └── layout.tsx
│   ├── game/                     # 게임 관련 페이지
│   │   ├── [bundleId]/          # 게임 플레이
│   │   └── compare/[shareCode]   # 결과 비교
│   ├── my/                       # 마이페이지
│   │   ├── questions/
│   │   ├── bundles/
│   │   └── history/
│   ├── explore/                  # 질문/묶음 탐색
│   ├── globals.css
│   ├── layout.tsx               # 루트 레이아웃
│   └── page.tsx                 # 홈페이지
├── components/                   # 재사용 컴포넌트
│   ├── ui/                      # 기본 UI 컴포넌트
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   └── ...
│   ├── game/                    # 게임 관련 컴포넌트
│   │   ├── GamePlay.tsx
│   │   ├── ResultComparison.tsx
│   │   └── ShareCodeInput.tsx
│   ├── admin/                   # 관리자 컴포넌트
│   │   ├── QuestionApproval.tsx
│   │   └── Dashboard.tsx
│   ├── common/                  # 공통 컴포넌트
│   │   ├── Header.tsx
│   │   ├── Navigation.tsx
│   │   └── Pagination.tsx
│   └── forms/                   # 폼 컴포넌트
│       ├── QuestionForm.tsx
│       └── BundleForm.tsx
├── lib/                         # 유틸리티 및 설정
│   ├── api/                     # API 클라이언트
│   │   ├── auth.ts
│   │   ├── game.ts
│   │   ├── questions.ts
│   │   └── admin.ts
│   ├── stores/                  # 상태 관리
│   │   ├── auth.ts
│   │   ├── game.ts
│   │   └── ui.ts
│   ├── types/                   # TypeScript 타입 정의
│   │   ├── api.ts
│   │   ├── game.ts
│   │   └── user.ts
│   ├── utils/                   # 유틸리티 함수
│   │   ├── cn.ts               # 클래스명 유틸
│   │   ├── format.ts           # 포맷팅
│   │   └── validation.ts       # 검증 스키마
│   └── constants.ts             # 상수 정의
├── hooks/                       # 커스텀 훅
│   ├── useAuth.ts
│   ├── usePagination.ts
│   └── useLocalStorage.ts
├── public/                      # 정적 파일
│   ├── icons/
│   └── images/
└── config files...
```

---

## 🎨 디자인 시스템

### Color Palette
```css
:root {
  /* Primary Colors - 밸런스 게임 특성에 맞는 대비 색상 */
  --primary-50: #eff6ff;
  --primary-500: #3b82f6;   /* 메인 브랜드 색상 */
  --primary-600: #2563eb;
  --primary-900: #1e3a8a;

  /* Secondary Colors - 선택지 구분용 */
  --red-500: #ef4444;       /* Option A */
  --blue-500: #3b82f6;     /* Option B */
  
  /* Semantic Colors */
  --success: #10b981;       /* 승인, 일치 */
  --warning: #f59e0b;       /* 대기 */
  --danger: #ef4444;        /* 거절, 불일치 */
  
  /* Neutral Colors */
  --gray-50: #f9fafb;
  --gray-100: #f3f4f6;
  --gray-500: #6b7280;
  --gray-900: #111827;
}
```

### Component Variants
```typescript
// components/ui/button.tsx
import { cva, type VariantProps } from "class-variance-authority";

const buttonVariants = cva(
  "inline-flex items-center justify-center rounded-lg font-medium transition-colors",
  {
    variants: {
      variant: {
        default: "bg-primary-500 text-white hover:bg-primary-600",
        outline: "border border-gray-300 bg-white hover:bg-gray-50",
        ghost: "hover:bg-gray-100",
        // 게임 전용 버튼
        optionA: "bg-red-500 text-white hover:bg-red-600 text-lg py-6",
        optionB: "bg-blue-500 text-white hover:bg-blue-600 text-lg py-6",
      },
      size: {
        sm: "h-9 px-3 text-sm",
        md: "h-10 px-4",
        lg: "h-12 px-6 text-lg",
        xl: "h-14 px-8 text-xl",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "md",
    },
  }
);
```

---

## 🔐 인증 시스템 구현

### 인증 Store (Zustand)
```typescript
// lib/stores/auth.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: number;
  email: string;
  nickname: string;
  role: 'USER' | 'ADMIN';
}

interface AuthState {
  user: User | null;
  isLoading: boolean;
  
  // Actions
  setUser: (user: User | null) => void;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
  
  // Computed
  isAuthenticated: boolean;
  isAdmin: boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isLoading: false,
      
      get isAuthenticated() {
        return !!get().user;
      },
      
      get isAdmin() {
        return get().user?.role === 'ADMIN';
      },
      
      setUser: (user) => set({ user }),
      
      login: async (email, password) => {
        set({ isLoading: true });
        try {
          await authAPI.login(email, password);
          const userInfo = await authAPI.getMe();
          set({ user: userInfo });
        } catch (error) {
          throw error;
        } finally {
          set({ isLoading: false });
        }
      },
      
      logout: async () => {
        try {
          await authAPI.logout();
        } finally {
          set({ user: null });
        }
      },
      
      checkAuth: async () => {
        set({ isLoading: true });
        try {
          const userInfo = await authAPI.getMe();
          set({ user: userInfo });
        } catch {
          set({ user: null });
        } finally {
          set({ isLoading: false });
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ user: state.user }), // user만 persist
    }
  )
);
```

### 인증 가드 컴포넌트
```typescript
// components/common/AuthGuard.tsx
'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/auth';

interface AuthGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireAdmin?: boolean;
  fallback?: React.ReactNode;
}

export function AuthGuard({ 
  children, 
  requireAuth = false, 
  requireAdmin = false,
  fallback 
}: AuthGuardProps) {
  const router = useRouter();
  const { user, isLoading, checkAuth } = useAuthStore();
  
  useEffect(() => {
    checkAuth();
  }, []);
  
  if (isLoading) {
    return <div>Loading...</div>;
  }
  
  if (requireAuth && !user) {
    if (fallback) return <>{fallback}</>;
    router.push('/login');
    return null;
  }
  
  if (requireAdmin && user?.role !== 'ADMIN') {
    if (fallback) return <>{fallback}</>;
    router.push('/');
    return null;
  }
  
  return <>{children}</>;
}
```

---

## 🎮 게임 플레이 구현

### 게임 상태 관리
```typescript
// lib/stores/game.ts
import { create } from 'zustand';

interface GameState {
  session: GameSession | null;
  currentQuestionIndex: number;
  answers: Record<number, 'A' | 'B'>;
  isPlaying: boolean;
  shareCode: string | null;
  
  // Actions
  startGame: (bundleId: number, tempUserId?: string) => Promise<void>;
  submitAnswer: (questionId: number, option: 'A' | 'B') => Promise<void>;
  nextQuestion: () => void;
  completeGame: () => Promise<string>;
  resetGame: () => void;
}

export const useGameStore = create<GameState>((set, get) => ({
  session: null,
  currentQuestionIndex: 0,
  answers: {},
  isPlaying: false,
  shareCode: null,
  
  startGame: async (bundleId, tempUserId) => {
    const session = await gameAPI.startGame(bundleId, tempUserId);
    set({ 
      session, 
      currentQuestionIndex: 0, 
      answers: {}, 
      isPlaying: true,
      shareCode: null 
    });
  },
  
  submitAnswer: async (questionId, option) => {
    const { session } = get();
    if (!session) return;
    
    await gameAPI.submitAnswer(session.sessionId, questionId, option);
    
    set(state => ({
      answers: { ...state.answers, [questionId]: option }
    }));
  },
  
  nextQuestion: () => {
    set(state => ({
      currentQuestionIndex: Math.min(
        state.currentQuestionIndex + 1,
        (state.session?.questions.length || 1) - 1
      )
    }));
  },
  
  completeGame: async () => {
    const { session } = get();
    if (!session) throw new Error('No active session');
    
    const shareCode = await gameAPI.completeGame(session.sessionId);
    set({ shareCode, isPlaying: false });
    return shareCode;
  },
  
  resetGame: () => {
    set({
      session: null,
      currentQuestionIndex: 0,
      answers: {},
      isPlaying: false,
      shareCode: null,
    });
  },
}));
```

### 게임 플레이 컴포넌트
```tsx
// components/game/GamePlay.tsx
'use client';

import { useEffect } from 'react';
import { useParams } from 'next/navigation';
import { useGameStore } from '@/lib/stores/game';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Card } from '@/components/ui/card';

export function GamePlay() {
  const { bundleId } = useParams();
  const {
    session,
    currentQuestionIndex,
    answers,
    isPlaying,
    startGame,
    submitAnswer,
    nextQuestion,
    completeGame,
  } = useGameStore();

  useEffect(() => {
    if (bundleId && !session) {
      const tempUserId = `temp-${Date.now()}-${Math.random()}`;
      startGame(Number(bundleId), tempUserId);
    }
  }, [bundleId, session, startGame]);

  if (!session || !isPlaying) {
    return <div className="flex justify-center p-8">게임을 준비하고 있습니다...</div>;
  }

  const currentQuestion = session.questions[currentQuestionIndex];
  const progress = ((currentQuestionIndex + 1) / session.questions.length) * 100;
  const isLastQuestion = currentQuestionIndex === session.questions.length - 1;

  const handleAnswer = async (option: 'A' | 'B') => {
    await submitAnswer(currentQuestion.id, option);
    
    if (isLastQuestion) {
      const shareCode = await completeGame();
      // 결과 페이지로 이동
      window.location.href = `/game/result?shareCode=${shareCode}`;
    } else {
      nextQuestion();
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      {/* 진행률 표시 */}
      <div className="mb-8">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>문제 {currentQuestionIndex + 1} / {session.questions.length}</span>
          <span>{Math.round(progress)}% 완료</span>
        </div>
        <Progress value={progress} className="h-2" />
      </div>

      {/* 질문 카드 */}
      <Card className="p-8 mb-8">
        <h1 className="text-2xl md:text-3xl font-bold text-center mb-8">
          {currentQuestion.text}
        </h1>
        
        <div className="grid md:grid-cols-2 gap-6">
          {/* 선택지 A */}
          <Button
            variant="optionA"
            size="xl"
            onClick={() => handleAnswer('A')}
            className="h-auto p-6 flex flex-col items-center space-y-4"
          >
            {currentQuestion.optionAImageUrl && (
              <img
                src={currentQuestion.optionAImageUrl}
                alt="Option A"
                className="w-full max-w-sm h-48 object-cover rounded-lg"
              />
            )}
            <span className="text-xl font-semibold">
              {currentQuestion.optionAText}
            </span>
          </Button>

          {/* 선택지 B */}
          <Button
            variant="optionB"
            size="xl"
            onClick={() => handleAnswer('B')}
            className="h-auto p-6 flex flex-col items-center space-y-4"
          >
            {currentQuestion.optionBImageUrl && (
              <img
                src={currentQuestion.optionBImageUrl}
                alt="Option B"
                className="w-full max-w-sm h-48 object-cover rounded-lg"
              />
            )}
            <span className="text-xl font-semibold">
              {currentQuestion.optionBText}
            </span>
          </Button>
        </div>
      </Card>

      {/* 이전 답변들 표시 (선택사항) */}
      <div className="flex justify-center space-x-2">
        {session.questions.slice(0, currentQuestionIndex).map((q, idx) => (
          <div
            key={q.id}
            className={`w-3 h-3 rounded-full ${
              answers[q.id] === 'A' ? 'bg-red-400' : 'bg-blue-400'
            }`}
            title={`문제 ${idx + 1}: ${answers[q.id]}`}
          />
        ))}
      </div>
    </div>
  );
}
```

---

## 📊 마이페이지 구현

### 내 통계 대시보드
```tsx
// app/my/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AuthGuard } from '@/components/common/AuthGuard';
import { myPageAPI } from '@/lib/api/mypage';

interface MyStats {
  totalQuestions: number;
  approvedQuestions: number;
  pendingQuestions: number;
  rejectedQuestions: number;
  totalBundles: number;
  totalGamesPlayed: number;
}

export default function MyPage() {
  const [stats, setStats] = useState<MyStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadStats = async () => {
      try {
        const data = await myPageAPI.getStats();
        setStats(data);
      } catch (error) {
        console.error('Failed to load stats:', error);
      } finally {
        setLoading(false);
      }
    };

    loadStats();
  }, []);

  return (
    <AuthGuard requireAuth>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-8">마이페이지</h1>
        
        {loading ? (
          <div>로딩 중...</div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {/* 질문 통계 */}
            <Card>
              <CardHeader>
                <CardTitle>내가 만든 질문</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold mb-4">{stats?.totalQuestions}</div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>승인됨</span>
                    <Badge variant="success">{stats?.approvedQuestions}</Badge>
                  </div>
                  <div className="flex justify-between">
                    <span>대기중</span>
                    <Badge variant="warning">{stats?.pendingQuestions}</Badge>
                  </div>
                  <div className="flex justify-between">
                    <span>거절됨</span>
                    <Badge variant="destructive">{stats?.rejectedQuestions}</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* 묶음 통계 */}
            <Card>
              <CardHeader>
                <CardTitle>내가 만든 묶음</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{stats?.totalBundles}</div>
                <p className="text-gray-600">질문 묶음</p>
              </CardContent>
            </Card>

            {/* 게임 통계 */}
            <Card>
              <CardHeader>
                <CardTitle>게임 플레이</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{stats?.totalGamesPlayed}</div>
                <p className="text-gray-600">총 플레이 횟수</p>
              </CardContent>
            </Card>
          </div>
        )}

        {/* 빠른 액션 버튼들 */}
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card className="hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 text-center">
              <h3 className="font-semibold mb-2">새 질문 만들기</h3>
              <p className="text-sm text-gray-600">관리자 승인 후 활성화</p>
            </CardContent>
          </Card>
          
          <Card className="hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 text-center">
              <h3 className="font-semibold mb-2">새 묶음 만들기</h3>
              <p className="text-sm text-gray-600">승인된 질문들로 구성</p>
            </CardContent>
          </Card>
          
          <Card className="hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 text-center">
              <h3 className="font-semibold mb-2">내 질문 관리</h3>
              <p className="text-sm text-gray-600">승인 상태 확인</p>
            </CardContent>
          </Card>
          
          <Card className="hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 text-center">
              <h3 className="font-semibold mb-2">게임 기록</h3>
              <p className="text-sm text-gray-600">플레이 이력 조회</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </AuthGuard>
  );
}
```

---

## 🛡️ 관리자 페이지 구현

### 질문 승인 관리
```tsx
// app/admin/questions/page.tsx
'use client';

import { useState } from 'react';
import { AuthGuard } from '@/components/common/AuthGuard';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { usePagination } from '@/hooks/usePagination';
import { adminAPI } from '@/lib/api/admin';

export default function AdminQuestionsPage() {
  const [rejectDialog, setRejectDialog] = useState<{
    open: boolean;
    questionId: number | null;
  }>({ open: false, questionId: null });
  const [rejectReason, setRejectReason] = useState('');
  const [selectedQuestions, setSelectedQuestions] = useState<number[]>([]);

  const {
    data: questions,
    loading,
    refresh,
    currentPage,
    totalPages,
    goToPage,
  } = usePagination({
    fetchData: adminAPI.getPendingQuestions,
    initialSize: 10,
  });

  const handleApprove = async (questionId: number) => {
    try {
      await adminAPI.approveQuestion(questionId);
      refresh();
    } catch (error) {
      console.error('승인 실패:', error);
    }
  };

  const handleReject = async () => {
    if (!rejectDialog.questionId || !rejectReason.trim()) return;
    
    try {
      await adminAPI.rejectQuestion(rejectDialog.questionId, rejectReason);
      setRejectDialog({ open: false, questionId: null });
      setRejectReason('');
      refresh();
    } catch (error) {
      console.error('거절 실패:', error);
    }
  };

  const handleBulkApprove = async () => {
    if (selectedQuestions.length === 0) return;
    
    try {
      await adminAPI.bulkApproveQuestions(selectedQuestions);
      setSelectedQuestions([]);
      refresh();
    } catch (error) {
      console.error('일괄 승인 실패:', error);
    }
  };

  return (
    <AuthGuard requireAdmin>
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">질문 승인 관리</h1>
          
          {selectedQuestions.length > 0 && (
            <Button onClick={handleBulkApprove}>
              선택된 {selectedQuestions.length}개 일괄 승인
            </Button>
          )}
        </div>

        {loading ? (
          <div>로딩 중...</div>
        ) : (
          <div className="space-y-4">
            {questions.map((question) => (
              <Card key={question.id} className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-4 mb-4">
                      <input
                        type="checkbox"
                        checked={selectedQuestions.includes(question.id)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setSelectedQuestions(prev => [...prev, question.id]);
                          } else {
                            setSelectedQuestions(prev => 
                              prev.filter(id => id !== question.id)
                            );
                          }
                        }}
                        className="w-4 h-4"
                      />
                      <Badge variant="warning">대기중</Badge>
                      <span className="text-sm text-gray-600">
                        {question.keyword}
                      </span>
                    </div>
                    
                    <h3 className="text-xl font-semibold mb-4">
                      {question.text}
                    </h3>
                    
                    <div className="grid md:grid-cols-2 gap-4 mb-4">
                      <div className="border rounded-lg p-4">
                        <div className="text-sm text-gray-600 mb-2">선택지 A</div>
                        {question.optionAImageUrl && (
                          <img 
                            src={question.optionAImageUrl} 
                            alt="Option A"
                            className="w-full h-32 object-cover rounded mb-2"
                          />
                        )}
                        <div className="font-medium">{question.optionAText}</div>
                      </div>
                      
                      <div className="border rounded-lg p-4">
                        <div className="text-sm text-gray-600 mb-2">선택지 B</div>
                        {question.optionBImageUrl && (
                          <img 
                            src={question.optionBImageUrl} 
                            alt="Option B"
                            className="w-full h-32 object-cover rounded mb-2"
                          />
                        )}
                        <div className="font-medium">{question.optionBText}</div>
                      </div>
                    </div>
                    
                    <div className="text-sm text-gray-600">
                      작성자: {question.creatorEmail} | 
                      작성일: {new Date(question.createdDate).toLocaleDateString()}
                    </div>
                  </div>
                  
                  <div className="flex space-x-2 ml-4">
                    <Button
                      variant="default"
                      size="sm"
                      onClick={() => handleApprove(question.id)}
                    >
                      승인
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setRejectDialog({ 
                        open: true, 
                        questionId: question.id 
                      })}
                    >
                      거절
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}

        {/* 페이지네이션 */}
        <div className="flex justify-center mt-8">
          <div className="flex space-x-2">
            {Array.from({ length: totalPages }, (_, i) => (
              <Button
                key={i}
                variant={currentPage === i ? "default" : "outline"}
                size="sm"
                onClick={() => goToPage(i)}
              >
                {i + 1}
              </Button>
            ))}
          </div>
        </div>

        {/* 거절 사유 입력 다이얼로그 */}
        <Dialog
          open={rejectDialog.open}
          onOpenChange={(open) => {
            if (!open) {
              setRejectDialog({ open: false, questionId: null });
              setRejectReason('');
            }
          }}
        >
          <DialogContent>
            <DialogHeader>
              <DialogTitle>질문 거절 사유</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <Textarea
                placeholder="거절 사유를 입력해주세요..."
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                rows={4}
              />
              <div className="flex justify-end space-x-2">
                <Button
                  variant="outline"
                  onClick={() => setRejectDialog({ open: false, questionId: null })}
                >
                  취소
                </Button>
                <Button
                  variant="destructive"
                  onClick={handleReject}
                  disabled={!rejectReason.trim()}
                >
                  거절
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    </AuthGuard>
  );
}
```

---

## 📱 모바일 최적화

### 반응형 게임 플레이
```css
/* globals.css - 게임 전용 스타일 */
.game-option-button {
  @apply transition-all duration-200 transform hover:scale-105 active:scale-95;
}

/* 모바일에서 터치 친화적인 크기 */
@media (max-width: 768px) {
  .game-option-button {
    @apply min-h-[120px] text-lg font-semibold;
  }
  
  .question-text {
    @apply text-xl leading-tight;
  }
}

/* 터치 디바이스 전용 스타일 */
@media (hover: none) and (pointer: coarse) {
  .game-option-button:hover {
    @apply scale-100; /* 호버 효과 비활성화 */
  }
  
  .game-option-button:active {
    @apply scale-95 bg-opacity-80;
  }
}
```

### 모바일 네비게이션
```tsx
// components/common/MobileNavigation.tsx
'use client';

import { useState } from 'react';
import { Menu, X, Home, User, Settings, GamepadIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { useAuthStore } from '@/lib/stores/auth';

export function MobileNavigation() {
  const [isOpen, setIsOpen] = useState(false);
  const { user, isAuthenticated, isAdmin } = useAuthStore();

  const menuItems = [
    { icon: Home, label: '홈', href: '/' },
    { icon: GamepadIcon, label: '게임 탐색', href: '/explore' },
    ...(isAuthenticated ? [
      { icon: User, label: '마이페이지', href: '/my' },
      ...(isAdmin ? [
        { icon: Settings, label: '관리자', href: '/admin' },
      ] : []),
    ] : []),
  ];

  return (
    <div className="md:hidden">
      <Sheet open={isOpen} onOpenChange={setIsOpen}>
        <SheetTrigger asChild>
          <Button variant="ghost" size="sm">
            <Menu className="h-6 w-6" />
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="w-80">
          <div className="flex flex-col h-full">
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-xl font-bold">Balance Game</h2>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsOpen(false)}
              >
                <X className="h-6 w-6" />
              </Button>
            </div>
            
            <nav className="flex-1">
              <ul className="space-y-2">
                {menuItems.map((item) => (
                  <li key={item.href}>
                    <a
                      href={item.href}
                      className="flex items-center space-x-3 p-3 rounded-lg hover:bg-gray-100 transition-colors"
                      onClick={() => setIsOpen(false)}
                    >
                      <item.icon className="h-5 w-5" />
                      <span>{item.label}</span>
                    </a>
                  </li>
                ))}
              </ul>
            </nav>
            
            <div className="border-t pt-4">
              {isAuthenticated ? (
                <div className="space-y-2">
                  <div className="text-sm text-gray-600">
                    안녕하세요, {user?.nickname}님
                  </div>
                  <Button variant="outline" size="sm" className="w-full">
                    로그아웃
                  </Button>
                </div>
              ) : (
                <div className="space-y-2">
                  <Button size="sm" className="w-full">
                    로그인
                  </Button>
                  <Button variant="outline" size="sm" className="w-full">
                    회원가입
                  </Button>
                </div>
              )}
            </div>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}
```

---

## 🔄 실시간 업데이트 (선택사항)

### Server-Sent Events로 승인 알림
```typescript
// hooks/useSSE.ts
import { useEffect, useState } from 'react';

export function useSSE(url: string, options?: { enabled?: boolean }) {
  const [data, setData] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!options?.enabled) return;

    const eventSource = new EventSource(url, {
      withCredentials: true,
    });

    eventSource.onmessage = (event) => {
      try {
        const parsedData = JSON.parse(event.data);
        setData(parsedData);
      } catch {
        setData(event.data);
      }
    };

    eventSource.onerror = () => {
      setError('연결 오류가 발생했습니다.');
    };

    return () => {
      eventSource.close();
    };
  }, [url, options?.enabled]);

  return { data, error };
}

// 사용 예시 - 마이페이지에서 질문 승인 알림
function MyQuestionsPage() {
  const { user } = useAuthStore();
  const { data: notification } = useSSE('/api/my/notifications/stream', {
    enabled: !!user,
  });

  useEffect(() => {
    if (notification?.type === 'QUESTION_APPROVED') {
      toast.success(`질문이 승인되었습니다: ${notification.questionText}`);
    } else if (notification?.type === 'QUESTION_REJECTED') {
      toast.error(`질문이 거절되었습니다: ${notification.reason}`);
    }
  }, [notification]);
  
  // ... 컴포넌트 렌더링
}
```

---

## 🧪 테스트 전략

### 게임 플레이 테스트
```typescript
// __tests__/components/GamePlay.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { GamePlay } from '@/components/game/GamePlay';
import { useGameStore } from '@/lib/stores/game';

// Mock 설정
jest.mock('@/lib/stores/game');
jest.mock('next/navigation', () => ({
  useParams: () => ({ bundleId: '1' }),
}));

const mockGameStore = {
  session: {
    sessionId: 1,
    bundleId: 1,
    bundleTitle: '테스트 묶음',
    questions: [
      {
        id: 1,
        text: '테스트 질문',
        optionAText: '선택지 A',
        optionBText: '선택지 B',
      },
    ],
  },
  currentQuestionIndex: 0,
  answers: {},
  isPlaying: true,
  startGame: jest.fn(),
  submitAnswer: jest.fn(),
  nextQuestion: jest.fn(),
  completeGame: jest.fn().mockResolvedValue('ABC12345'),
};

describe('GamePlay Component', () => {
  beforeEach(() => {
    (useGameStore as jest.Mock).mockReturnValue(mockGameStore);
  });

  it('질문과 선택지를 올바르게 렌더링한다', () => {
    render(<GamePlay />);
    
    expect(screen.getByText('테스트 질문')).toBeInTheDocument();
    expect(screen.getByText('선택지 A')).toBeInTheDocument();
    expect(screen.getByText('선택지 B')).toBeInTheDocument();
  });

  it('선택지 클릭 시 답변이 제출된다', async () => {
    render(<GamePlay />);
    
    const optionA = screen.getByText('선택지 A');
    fireEvent.click(optionA);
    
    await waitFor(() => {
      expect(mockGameStore.submitAnswer).toHaveBeenCalledWith(1, 'A');
    });
  });

  it('마지막 질문에서 게임이 완료된다', async () => {
    // 마지막 질문 상태로 설정
    const lastQuestionStore = {
      ...mockGameStore,
      currentQuestionIndex: 0, // 질문이 1개뿐이므로 마지막
    };
    (useGameStore as jest.Mock).mockReturnValue(lastQuestionStore);
    
    // window.location.href 모킹
    delete (window as any).location;
    (window as any).location = { href: '' };
    
    render(<GamePlay />);
    
    const optionA = screen.getByText('선택지 A');
    fireEvent.click(optionA);
    
    await waitFor(() => {
      expect(mockGameStore.completeGame).toHaveBeenCalled();
      expect(window.location.href).toBe('/game/result?shareCode=ABC12345');
    });
  });
});
```

### API 테스트
```typescript
// __tests__/lib/api/game.test.ts
import { gameAPI } from '@/lib/api/game';

// Fetch 모킹
global.fetch = jest.fn();

describe('Game API', () => {
  beforeEach(() => {
    (fetch as jest.Mock).mockClear();
  });

  it('게임을 성공적으로 시작한다', async () => {
    const mockSession = {
      sessionId: 1,
      bundleId: 1,
      bundleTitle: '테스트 묶음',
      questions: [],
    };

    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockSession,
    });

    const result = await gameAPI.startGame(1, 'temp-123');

    expect(fetch).toHaveBeenCalledWith('/api/game/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        bundleId: 1,
        tempUserId: 'temp-123',
      }),
    });

    expect(result).toEqual(mockSession);
  });

  it('답변 제출이 성공한다', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
    });

    await gameAPI.submitAnswer(1, 1, 'A');

    expect(fetch).toHaveBeenCalledWith('/api/game/answer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        sessionId: 1,
        questionId: 1,
        selectedOption: 'A',
      }),
    });
  });
});
```

---

## 🚀 배포 및 최적화

### Next.js 최적화 설정
```javascript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  images: {
    domains: ['your-azure-storage.blob.core.windows.net'],
    formats: ['image/webp', 'image/avif'],
  },
  
  // API 프록시 (개발환경)
  async rewrites() {
    return process.env.NODE_ENV === 'development' ? [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ] : [];
  },
  
  // 성능 최적화
  experimental: {
    optimizeCss: true,
    optimizePackageImports: ['lucide-react'],
  },
  
  // PWA 설정 (선택사항)
  ...(process.env.NODE_ENV === 'production' && {
    async headers() {
      return [
        {
          source: '/(.*)',
          headers: [
            {
              key: 'X-Frame-Options',
              value: 'DENY',
            },
            {
              key: 'X-Content-Type-Options',
              value: 'nosniff',
            },
          ],
        },
      ];
    },
  }),
};

module.exports = nextConfig;
```

### 환경 변수 관리
```bash
# .env.local (개발)
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_URL=http://localhost:3000
NEXT_PUBLIC_ENVIRONMENT=development

# .env.production (운영)
NEXT_PUBLIC_API_URL=https://api.balancegame.com
NEXT_PUBLIC_APP_URL=https://balancegame.com
NEXT_PUBLIC_ENVIRONMENT=production
```

### 성능 모니터링
```typescript
// lib/analytics.ts
export const trackEvent = (name: string, properties?: Record<string, any>) => {
  if (typeof window !== 'undefined' && process.env.NODE_ENV === 'production') {
    // Google Analytics, Mixpanel 등 연동
    console.log('Event:', name, properties);
  }
};

// 게임 이벤트 추적
export const trackGameEvent = {
  start: (bundleId: number) => trackEvent('game_start', { bundleId }),
  answer: (questionId: number, option: string) => 
    trackEvent('game_answer', { questionId, option }),
  complete: (shareCode: string) => trackEvent('game_complete', { shareCode }),
  share: (shareCode: string) => trackEvent('game_share', { shareCode }),
};
```

---

## 📞 협업 가이드

### API 변경 대응
1. **백엔드 변경 사항 확인**: `API_Documentation.md` 정기 확인
2. **타입 정의 업데이트**: 새로운 API 응답에 맞춰 타입 수정
3. **에러 처리**: 새로운 에러 코드에 대한 처리 추가
4. **테스트 업데이트**: API 변경에 따른 테스트 케이스 수정

### 코드 리뷰 체크리스트
- [ ] TypeScript 타입 안정성 확인
- [ ] 인증이 필요한 API에 `credentials: 'include'` 포함
- [ ] 에러 처리 및 로딩 상태 구현
- [ ] 모바일 반응형 디자인 확인
- [ ] 접근성 (a11y) 고려
- [ ] 성능 최적화 (이미지, 번들 크기)

### 백엔드 개발자와의 소통
- **정기 미팅**: 주 2회 API 리뷰 및 이슈 논의
- **문서 공유**: API 변경사항 실시간 공유
- **테스트 협업**: 통합 테스트 시나리오 공동 작성
- **배포 조율**: 프론트엔드/백엔드 배포 일정 조율

---

*이 가이드는 Balance Game 프론트엔드 개발의 모든 측면을 다루는 종합 문서입니다. 프로젝트 진행에 따라 지속적으로 업데이트하여 최신 상태를 유지하세요.*