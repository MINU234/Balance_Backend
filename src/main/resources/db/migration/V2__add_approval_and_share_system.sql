-- =====================================================
-- 밸런스 게임 시스템 마이그레이션 스크립트
-- Version: 2.0
-- Description: 승인 시스템 및 공유 코드 시스템 추가
-- =====================================================

-- 1. 질문 승인 시스템 관련 컬럼 추가
-- -----------------------------------------------------
ALTER TABLE question
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500),
ADD COLUMN IF NOT EXISTS approved_by BIGINT,
ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;

-- 외래키 제약조건 추가
ALTER TABLE question
ADD CONSTRAINT fk_question_approved_by 
FOREIGN KEY (approved_by) REFERENCES "user" (user_id) ON DELETE SET NULL;

-- 기존 질문들을 모두 승인된 상태로 업데이트 (기존 데이터 보존)
UPDATE question 
SET approval_status = 'APPROVED',
    approved_at = NOW()
WHERE approval_status = 'PENDING';

-- 2. GameSession 테이블 수정 - 개인 플레이 및 공유 코드 시스템
-- -----------------------------------------------------

-- 기존 컬럼 백업 (데이터가 있을 경우를 대비)
ALTER TABLE game_session 
ADD COLUMN IF NOT EXISTS player_user_id BIGINT,
ADD COLUMN IF NOT EXISTS share_code VARCHAR(8) UNIQUE,
ADD COLUMN IF NOT EXISTS parent_session_id BIGINT,
ADD COLUMN IF NOT EXISTS temp_user_id VARCHAR(50),
ADD COLUMN IF NOT EXISTS share_code_created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS share_code_expires_at TIMESTAMP;

-- 기존 player_1, player_2 데이터를 player_user_id로 마이그레이션
UPDATE game_session 
SET player_user_id = player_1_user_id 
WHERE player_user_id IS NULL AND player_1_user_id IS NOT NULL;

-- 기존 컬럼 삭제 (안전하게 처리)
ALTER TABLE game_session 
DROP COLUMN IF EXISTS player_1_user_id CASCADE,
DROP COLUMN IF EXISTS player_2_user_id CASCADE;

-- session_status 타입 변경 및 기본값 설정
ALTER TABLE game_session
ALTER COLUMN session_status TYPE VARCHAR(20),
ALTER COLUMN session_status SET DEFAULT 'IN_PROGRESS';

-- 외래키 제약조건 추가
ALTER TABLE game_session
ADD CONSTRAINT fk_game_session_player 
    FOREIGN KEY (player_user_id) REFERENCES "user" (user_id) ON DELETE SET NULL,
ADD CONSTRAINT fk_game_session_parent 
    FOREIGN KEY (parent_session_id) REFERENCES game_session (session_id) ON DELETE CASCADE;

-- 3. 인덱스 생성 (성능 최적화)
-- -----------------------------------------------------

-- 질문 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_question_approval_status 
    ON question(approval_status);
CREATE INDEX IF NOT EXISTS idx_question_creator_approval 
    ON question(creator_id, approval_status);
CREATE INDEX IF NOT EXISTS idx_question_approved_at 
    ON question(approved_at DESC);
CREATE INDEX IF NOT EXISTS idx_question_keyword_approval 
    ON question(keyword, approval_status) WHERE approval_status = 'APPROVED';

-- 게임 세션 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_game_session_share_code 
    ON game_session(share_code) WHERE share_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_game_session_parent 
    ON game_session(parent_session_id) WHERE parent_session_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_game_session_temp_user 
    ON game_session(temp_user_id) WHERE temp_user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_game_session_player 
    ON game_session(player_user_id);
CREATE INDEX IF NOT EXISTS idx_game_session_expires 
    ON game_session(share_code_expires_at) WHERE share_code_expires_at IS NOT NULL;

-- 4. 통계 테이블 확인 및 수정
-- -----------------------------------------------------

-- question_stats 테이블 확인
CREATE TABLE IF NOT EXISTS question_stats (
    question_id BIGINT PRIMARY KEY REFERENCES question(question_id) ON DELETE CASCADE,
    option_a_count BIGINT NOT NULL DEFAULT 0,
    option_b_count BIGINT NOT NULL DEFAULT 0,
    total_count BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- question_bundle_stats 테이블 확인  
CREATE TABLE IF NOT EXISTS question_bundle_stats (
    bundle_id BIGINT PRIMARY KEY REFERENCES question_bundle(bundle_id) ON DELETE CASCADE,
    play_count BIGINT NOT NULL DEFAULT 0
);

-- 5. 관리자 대시보드를 위한 뷰 생성
-- -----------------------------------------------------

-- 승인 대기 질문 통계 뷰
CREATE OR REPLACE VIEW v_pending_questions_stats AS
SELECT 
    COUNT(*) as total_pending,
    COUNT(DISTINCT creator_id) as unique_creators,
    DATE(created_at) as created_date
FROM question
WHERE approval_status = 'PENDING'
GROUP BY DATE(created_at)
ORDER BY created_date DESC;

-- 관리자 활동 통계 뷰
CREATE OR REPLACE VIEW v_admin_activity_stats AS
SELECT 
    u.nickname as admin_name,
    COUNT(CASE WHEN q.approval_status = 'APPROVED' THEN 1 END) as approved_count,
    COUNT(CASE WHEN q.approval_status = 'REJECTED' THEN 1 END) as rejected_count,
    DATE(q.approved_at) as activity_date
FROM question q
JOIN "user" u ON q.approved_by = u.user_id
WHERE q.approved_at IS NOT NULL
GROUP BY u.nickname, DATE(q.approved_at)
ORDER BY activity_date DESC;

-- 6. 기본 관리자 계정 생성 (없을 경우)
-- -----------------------------------------------------

-- 관리자 계정이 없으면 생성 (비밀번호는 BCrypt로 'admin123' 암호화된 값)
INSERT INTO "user" (email, password, nickname, role, created_at, updated_at)
SELECT 
    'admin@balancegame.com',
    '$2a$10$8K1p/a/zPYp5.L8UG8pXOuLqE5LHG8TGY4YoFJ2mDVLHaKL9hTWmG',
    '시스템관리자',
    'ADMIN',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM "user" WHERE role = 'ADMIN'
);

-- 7. 데이터 무결성 확인
-- -----------------------------------------------------

-- 통계 테이블에 누락된 데이터 생성
INSERT INTO question_stats (question_id, option_a_count, option_b_count, total_count, updated_at)
SELECT q.question_id, 0, 0, 0, NOW()
FROM question q
LEFT JOIN question_stats qs ON q.question_id = qs.question_id
WHERE qs.question_id IS NULL;

INSERT INTO question_bundle_stats (bundle_id, play_count)
SELECT qb.bundle_id, 0
FROM question_bundle qb
LEFT JOIN question_bundle_stats qbs ON qb.bundle_id = qbs.bundle_id
WHERE qbs.bundle_id IS NULL;

-- 8. 공유 코드 만료 처리를 위한 함수 생성
-- -----------------------------------------------------

CREATE OR REPLACE FUNCTION expire_old_share_codes()
RETURNS void AS $$
BEGIN
    UPDATE game_session
    SET share_code = NULL
    WHERE share_code_expires_at < NOW()
    AND share_code IS NOT NULL;
END;
$$ LANGUAGE plpgsql;

-- 9. 정기적인 만료 처리를 위한 크론잡 설정 (옵션)
-- PostgreSQL의 pg_cron extension이 설치되어 있다면 사용
-- CREATE EXTENSION IF NOT EXISTS pg_cron;
-- SELECT cron.schedule('expire-share-codes', '0 0 * * *', 'SELECT expire_old_share_codes();');

-- 10. 롤백을 위한 정보 코멘트
-- -----------------------------------------------------
COMMENT ON COLUMN question.approval_status IS '질문 승인 상태: PENDING(대기), APPROVED(승인), REJECTED(거절)';
COMMENT ON COLUMN question.rejection_reason IS '거절 사유';
COMMENT ON COLUMN question.approved_by IS '승인/거절한 관리자 ID';
COMMENT ON COLUMN question.approved_at IS '승인/거절 일시';

COMMENT ON COLUMN game_session.share_code IS '8자리 공유 코드';
COMMENT ON COLUMN game_session.share_code_created_at IS '공유 코드 생성 일시';
COMMENT ON COLUMN game_session.share_code_expires_at IS '공유 코드 만료 일시';
COMMENT ON COLUMN game_session.parent_session_id IS '비교 대상 원본 세션 ID';
COMMENT ON COLUMN game_session.temp_user_id IS '비회원 임시 식별자';

-- 마이그레이션 완료 메시지
DO $$
BEGIN
    RAISE NOTICE '마이그레이션 V2 완료: 승인 시스템 및 공유 코드 시스템이 성공적으로 적용되었습니다.';
END $$;