-- V5: 성능 최적화를 위한 인덱스 추가

-- 1. 질문 관련 인덱스
-- 승인 상태와 키워드 조합으로 자주 검색되는 패턴
CREATE INDEX IF NOT EXISTS idx_question_approval_keyword 
ON question(approval_status, keyword);

-- 생성자별 질문 조회 최적화
CREATE INDEX IF NOT EXISTS idx_question_creator_created 
ON question(creator_id, created_date DESC);

-- 2. 질문 묶음 관련 인덱스  
-- 공개 묶음 조회 최적화
CREATE INDEX IF NOT EXISTS idx_bundle_public_created 
ON question_bundle(is_public, created_date DESC);

-- 생성자별 묶음 조회 최적화
CREATE INDEX IF NOT EXISTS idx_bundle_creator_created 
ON question_bundle(creator_id, created_date DESC);

-- 3. 게임 세션 관련 인덱스
-- 묶음별 세션 조회 및 상태 필터링
CREATE INDEX IF NOT EXISTS idx_session_bundle_status 
ON game_session(bundle_id, session_status);

-- 사용자별 게임 기록 조회 최적화
CREATE INDEX IF NOT EXISTS idx_session_player_created 
ON game_session(player_user_id, created_at DESC);

-- 임시 사용자 세션 조회
CREATE INDEX IF NOT EXISTS idx_session_temp_user_created 
ON game_session(temp_user_id, created_at DESC);

-- 공유코드 만료 시간 기반 인덱스 (스케줄러 최적화)
CREATE INDEX IF NOT EXISTS idx_session_share_expires 
ON game_session(share_code_expires_at) 
WHERE share_code IS NOT NULL;

-- 4. 사용자 답변 관련 인덱스
-- 세션-질문 조합 (중복 답변 체크 및 결과 조회)
CREATE INDEX IF NOT EXISTS idx_answer_session_question 
ON user_answer(game_session_id, question_id);

-- 질문별 답변 통계 조회
CREATE INDEX IF NOT EXISTS idx_answer_question_option 
ON user_answer(question_id, selected_option);

-- 5. 통계 관련 인덱스
-- 질문 통계 조회
CREATE INDEX IF NOT EXISTS idx_question_stats_counts 
ON question_stats(option_a_count + option_b_count DESC);

-- 묶음 통계 조회
CREATE INDEX IF NOT EXISTS idx_bundle_stats_play_count 
ON question_bundle_stats(play_count DESC);

-- 6. 사용자 관련 인덱스
-- 이메일 중복 체크 (이미 unique 제약이 있지만 성능 향상)
CREATE INDEX IF NOT EXISTS idx_user_email_role 
ON users(email, role);

-- 닉네임 검색
CREATE INDEX IF NOT EXISTS idx_user_nickname 
ON users(nickname);

-- 7. 복합 인덱스 (자주 함께 사용되는 조건들)
-- 승인된 질문의 인기도 순 정렬
CREATE INDEX IF NOT EXISTS idx_question_approved_popular 
ON question(approval_status, created_date DESC) 
WHERE approval_status = 'APPROVED';

-- 공개 묶음의 인기도 순 정렬  
CREATE INDEX IF NOT EXISTS idx_bundle_public_popular 
ON question_bundle(is_public, created_date DESC) 
WHERE is_public = true;

-- 8. 텍스트 검색 최적화 (PostgreSQL의 경우)
-- 질문 텍스트 전문 검색
CREATE INDEX IF NOT EXISTS idx_question_text_search 
ON question USING gin(to_tsvector('korean', text));

-- 묶음 제목/설명 전문 검색
CREATE INDEX IF NOT EXISTS idx_bundle_title_search 
ON question_bundle USING gin(to_tsvector('korean', title || ' ' || description));

-- 인덱스 생성 완료 로그
INSERT INTO flyway_schema_history (version, description, type, script, checksum, installed_by, execution_time, success)
VALUES ('5', 'Add performance indexes', 'SQL', 'V5__add_performance_indexes.sql', 0, 'system', 0, true)
ON CONFLICT DO NOTHING;