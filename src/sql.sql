-- 1. 사용자(User) 테이블
CREATE TABLE "user" (
                        "user_id" BIGSERIAL PRIMARY KEY,
                        "provider" VARCHAR(50),
                        "provider_id" VARCHAR(255),
                        "email" VARCHAR(255) UNIQUE,
                        "password" VARCHAR(255),
                        "nickname" VARCHAR(50) NOT NULL UNIQUE,
                        "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        "updated_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT "uk_user_provider_and_id" UNIQUE ("provider", "provider_id")
);
COMMENT ON TABLE "user" IS '서비스 사용자 정보를 저장하는 테이블';

-- 2. 질문(Question) 테이블
CREATE TABLE "question" (
                            "question_id" BIGSERIAL PRIMARY KEY,
                            "text" VARCHAR(500) NOT NULL,
                            "option_a_text" VARCHAR(255) NOT NULL,
                            "option_b_text" VARCHAR(255) NOT NULL,
                            "keyword" VARCHAR(50),
                            "creator_id" BIGINT REFERENCES "user"("user_id"),
                            "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
                            "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            "updated_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "question" IS '밸런스 게임의 원본 질문 데이터를 저장하는 테이블';

-- 3. 질문 묶음(QuestionBundle) 테이블
CREATE TABLE "question_bundle" (
                                   "bundle_id" BIGSERIAL PRIMARY KEY,
                                   "title" VARCHAR(100) NOT NULL,
                                   "description" VARCHAR(500),
                                   "creator_id" BIGINT NOT NULL REFERENCES "user"("user_id"),
                                   "is_public" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   "updated_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "question_bundle" IS '사용자가 생성한 질문 묶음(컬렉션) 정보를 저장하는 테이블';

-- 4. 묶음-질문 연결(BundleQuestion) 테이블
CREATE TABLE "bundle_question" (
                                   "bundle_question_id" BIGSERIAL PRIMARY KEY,
                                   "bundle_id" BIGINT NOT NULL REFERENCES "question_bundle"("bundle_id") ON DELETE CASCADE,
                                   "question_id" BIGINT NOT NULL REFERENCES "question"("question_id"),
                                   "order_index" INT NOT NULL, --
                                   UNIQUE("bundle_id", "question_id")
);
COMMENT ON TABLE "bundle_question" IS '질문 묶음과 질문 간의 다대다 관계를 위한 조인 테이블';

-- 5. 사용자 답변(UserAnswer) 테이블
CREATE TABLE "user_answer" (
                               "answer_id" BIGSERIAL PRIMARY KEY,
                               "user_id" BIGINT REFERENCES "user"("user_id"), -- 답변한 사용자 (비회원인 경우 NULL)
                               "question_id" BIGINT NOT NULL REFERENCES "question"("question_id"),
                               "selected_option" CHAR(1) NOT NULL CHECK ("selected_option" IN ('A', 'B')),
                               "answered_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "user_answer" IS '모든 플레이어의 답변을 기록하여 통계에 활용하는 테이블';

-- 6. 질문 통계(QuestionStats) 테이블
CREATE TABLE "question_stats" (
                                  "question_id" BIGINT PRIMARY KEY REFERENCES "question"("question_id") ON DELETE CASCADE,
                                  "option_a_count" BIGINT NOT NULL DEFAULT 0,
                                  "option_b_count" BIGINT NOT NULL DEFAULT 0,
                                  "total_count" BIGINT NOT NULL DEFAULT 0,
                                  "updated_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "question_stats" IS '각 질문의 선택지별 전체 응답 횟수를 집계하는 통계 테이블';
