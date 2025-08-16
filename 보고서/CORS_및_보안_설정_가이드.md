# 🔒 Balance Game CORS 및 보안 설정 가이드

## 📍 현재 서비스 정보
- **백엔드 서버**: https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net
- **배포 환경**: Microsoft Azure App Service
- **보안 설정**: 2025년 1월 14일 강화 완료 ✅

## 🌐 현재 CORS 설정

### 허용된 Origin
```
https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net
```

### 허용된 HTTP 메소드
```
GET, POST, PUT, DELETE, OPTIONS
```

### 허용된 헤더 (보안 강화)
```
Content-Type
Authorization
X-Requested-With
Cache-Control
```
**⚠️ 기존 `*` (모든 헤더 허용)에서 특정 헤더만 허용으로 변경**

---

## 🔧 프론트엔드에서 CORS 대응

### 1. Fetch API 설정
```typescript
// 올바른 설정
const apiCall = async () => {
  const response = await fetch('https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net/api/endpoint', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest', // CORS preflight 처리
    },
    credentials: 'include', // 쿠키 포함 (JWT 인증)
    body: JSON.stringify(data)
  });
};

// ❌ 잘못된 설정 - 허용되지 않은 헤더
const wrongApiCall = async () => {
  const response = await fetch('/api/endpoint', {
    headers: {
      'Content-Type': 'application/json',
      'Custom-Header': 'value', // ❌ 허용되지 않음
      'Accept': '*/*' // ❌ 허용되지 않음
    }
  });
};
```

### 2. Axios 설정
```typescript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net',
  withCredentials: true,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
});

// 요청 인터셉터
apiClient.interceptors.request.use((config) => {
  // 허용된 헤더만 설정
  const allowedHeaders = [
    'Content-Type',
    'Authorization', 
    'X-Requested-With',
    'Cache-Control'
  ];
  
  // 허용되지 않은 헤더 제거
  Object.keys(config.headers).forEach(header => {
    if (!allowedHeaders.includes(header)) {
      delete config.headers[header];
    }
  });
  
  return config;
});
```

---

## 🏗️ 개발환경 프록시 설정

### Next.js (프로덕션용)
```javascript
// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return process.env.NODE_ENV === 'development' ? [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*' // 로컬 개발용
      }
    ] : [];
  },
  
  // 프로덕션에서는 직접 API 호출
  env: {
    NEXT_PUBLIC_API_URL: process.env.NODE_ENV === 'production' 
      ? 'https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net'
      : 'http://localhost:8080'
  }
};

module.exports = nextConfig;
```

### Vite (React)
```typescript
// vite.config.ts
import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  
  define: {
    __API_URL__: JSON.stringify(
      process.env.NODE_ENV === 'production'
        ? 'https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net'
        : 'http://localhost:8080'
    )
  }
});
```

---

## 🔒 JWT 인증 보안 설정

### HttpOnly 쿠키 인증
백엔드에서 JWT를 HttpOnly 쿠키로 설정하므로 프론트엔드에서 토큰을 직접 관리할 필요가 없습니다.

```typescript
// ✅ 올바른 인증 방식
const authenticatedRequest = async () => {
  const response = await fetch('/api/protected-endpoint', {
    method: 'GET',
    credentials: 'include', // 쿠키 자동 포함
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    }
  });
  
  if (response.status === 401) {
    // 토큰 만료 - 로그인 페이지로 리다이렉트
    window.location.href = '/login';
    return;
  }
  
  return response.json();
};

// ❌ 잘못된 방식 - 토큰을 로컬스토리지에서 관리
const wrongAuthRequest = async () => {
  const token = localStorage.getItem('token'); // ❌ XSS 취약
  
  const response = await fetch('/api/protected-endpoint', {
    headers: {
      'Authorization': `Bearer ${token}` // ❌ 불필요
    }
  });
};
```

### 토큰 갱신 처리
```typescript
// 토큰 만료 시 자동 갱신
const handleTokenRefresh = async () => {
  try {
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      credentials: 'include'
    });
    
    if (!response.ok) {
      throw new Error('Token refresh failed');
    }
    
    return true;
  } catch (error) {
    // 갱신 실패 - 로그인 페이지로 리다이렉트
    window.location.href = '/login';
    return false;
  }
};

// API 클라이언트에 토큰 갱신 로직 추가
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshSuccess = await handleTokenRefresh();
      
      if (refreshSuccess) {
        // 토큰 갱신 성공 - 원래 요청 재시도
        return apiClient.request(error.config);
      }
    }
    
    return Promise.reject(error);
  }
);
```

---

## 🚨 보안 모범 사례

### 1. XSS 방지
```typescript
// ✅ HTML 이스케이프
import DOMPurify from 'dompurify';

const sanitizeHtml = (html: string) => {
  return DOMPurify.sanitize(html);
};

// React에서 사용
const SafeComponent = ({ userInput }: { userInput: string }) => {
  return (
    <div 
      dangerouslySetInnerHTML={{ 
        __html: sanitizeHtml(userInput) 
      }} 
    />
  );
};

// ❌ 위험한 방식
const UnsafeComponent = ({ userInput }: { userInput: string }) => {
  return <div dangerouslySetInnerHTML={{ __html: userInput }} />; // XSS 위험
};
```

### 2. CSRF 방지
```typescript
// CSRF 토큰이 필요한 경우 (현재는 REST API라 비활성화)
const csrfProtectedRequest = async () => {
  const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
  
  const response = await fetch('/api/sensitive-action', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-Token': csrfToken || '',
      'X-Requested-With': 'XMLHttpRequest'
    },
    credentials: 'include',
    body: JSON.stringify(data)
  });
};
```

### 3. 입력값 검증
```typescript
import { z } from 'zod';

// 프론트엔드 검증 스키마
const loginSchema = z.object({
  email: z.string().email('올바른 이메일을 입력하세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다')
});

const handleLogin = async (formData: FormData) => {
  try {
    const data = {
      email: formData.get('email') as string,
      password: formData.get('password') as string
    };
    
    // 클라이언트 사이드 검증
    const validatedData = loginSchema.parse(data);
    
    const response = await apiClient.post('/api/auth/login', validatedData);
    return response.data;
  } catch (error) {
    if (error instanceof z.ZodError) {
      console.error('입력값 검증 실패:', error.errors);
    }
    throw error;
  }
};
```

---

## 🔧 새 도메인 CORS 추가 요청

### 백엔드 CORS 설정 업데이트 방법
프론트엔드 도메인을 CORS에 추가하려면 다음 Azure CLI 명령을 실행해야 합니다:

```bash
# 프론트엔드 도메인 추가
az webapp config appsettings set \
  --name balance-game \
  --resource-group MINWOO \
  --settings \
    CORS_ALLOWED_ORIGINS="https://your-frontend-domain.com,https://www.your-frontend-domain.com,https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net"
```

### 도메인 추가 요청 템플릿
```markdown
**CORS 도메인 추가 요청**

- 도메인: https://your-frontend-domain.com
- 환경: 프로덕션 / 개발
- 사용 목적: Balance Game 프론트엔드 연동
- 요청자: [이름/팀명]
- 요청일: [날짜]

**확인사항:**
- [ ] HTTPS 인증서 적용 완료
- [ ] 도메인 소유권 확인
- [ ] 개발/스테이징/프로덕션 환경 구분
```

---

## 🧪 CORS 테스트 도구

### 브라우저에서 CORS 테스트
```javascript
// 브라우저 콘솔에서 실행
const testCors = async () => {
  try {
    const response = await fetch('https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net/actuator/health', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      credentials: 'include'
    });
    
    console.log('CORS 테스트 성공:', await response.json());
  } catch (error) {
    console.error('CORS 테스트 실패:', error);
  }
};

testCors();
```

### 프리플라이트 요청 확인
```javascript
// 복잡한 요청의 프리플라이트 확인
const testPreflight = async () => {
  try {
    const response = await fetch('https://balance-game-ekedbfhkcxeyc8cb.koreacentral-01.azurewebsites.net/api/game/start', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      body: JSON.stringify({ bundleId: 1, userEmail: null })
    });
    
    console.log('프리플라이트 성공:', response.status);
  } catch (error) {
    console.error('프리플라이트 실패:', error);
  }
};
```

---

## 🔍 CORS 오류 디버깅

### 일반적인 CORS 오류들

#### 1. "Access-Control-Allow-Origin" 오류
```
Access to fetch at 'https://api.example.com' from origin 'https://frontend.com' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present
```

**해결방법:**
- 백엔드에서 프론트엔드 도메인을 CORS에 추가
- 환경 변수 `CORS_ALLOWED_ORIGINS` 업데이트

#### 2. "Access-Control-Allow-Headers" 오류
```
Access to fetch at 'https://api.example.com' from origin 'https://frontend.com' 
has been blocked by CORS policy: Request header field 'custom-header' is not 
allowed by Access-Control-Allow-Headers
```

**해결방법:**
- 허용된 헤더만 사용: `Content-Type`, `Authorization`, `X-Requested-With`, `Cache-Control`
- 커스텀 헤더 제거

#### 3. "Access-Control-Allow-Credentials" 오류
```
Access to fetch at 'https://api.example.com' from origin 'https://frontend.com' 
has been blocked by CORS policy: The value of the 'Access-Control-Allow-Credentials' 
header in the response is '' which must be 'true'
```

**해결방법:**
- `credentials: 'include'` 사용 시 백엔드에서 `Access-Control-Allow-Credentials: true` 설정 필요

### 디버깅 체크리스트
- [ ] 프론트엔드 도메인이 CORS에 추가되었는가?
- [ ] 허용된 헤더만 사용하고 있는가?
- [ ] `credentials: 'include'` 설정이 올바른가?
- [ ] HTTP vs HTTPS 프로토콜이 일치하는가?
- [ ] 포트 번호가 정확한가?

---

## 📚 관련 리소스

### 공식 문서
- [MDN CORS 가이드](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring Security CORS 문서](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [Azure App Service CORS 설정](https://docs.microsoft.com/en-us/azure/app-service/app-service-web-tutorial-rest-api)

### 개발 도구
- [CORS 테스트 도구](https://cors-test.codehappy.dev/)
- [브라우저 개발자 도구 네트워크 탭](https://developer.chrome.com/docs/devtools/network/)

### Balance Game 관련 문서
- [API 실제 엔드포인트 가이드](./API_실제_엔드포인트_가이드.md)
- [프론트엔드 개발 가이드](./Frontend_Development_Guide.md)
- [프론트엔드 API 가이드](./Frontend_API_Guide.md)

---

## 📞 지원 및 문의

### CORS 관련 문의
- **기술 지원**: 백엔드 개발팀
- **도메인 추가 요청**: DevOps 팀  
- **보안 관련 문의**: 보안 담당자

### 긴급 상황 대응
1. **CORS 오류 발생 시**: 개발자 도구 Network 탭 확인
2. **새 도메인 추가 필요 시**: 위의 템플릿으로 요청
3. **보안 이슈 발견 시**: 즉시 보안팀에 보고

---

*마지막 업데이트: 2025년 1월 14일*  
*실제 운영 서버 보안 설정 기준으로 작성된 문서입니다.*