-- 테스트 데이터 삽입 스크립트
-- Balance Game 테스트 데이터 생성

-- 1. 테스트 사용자 생성
INSERT INTO "user" (email, password, nickname, role, created_at, updated_at)
VALUES 
    ('user1@test.com', '$2a$10$8K1p/a/zPYp5.L8UG8pXOuLqE5LHG8TGY4YoFJ2mDVLHaKL9hTWmG', '테스트유저1', 'USER', NOW(), NOW()),
    ('user2@test.com', '$2a$10$8K1p/a/zPYp5.L8UG8pXOuLqE5LHG8TGY4YoFJ2mDVLHaKL9hTWmG', '테스트유저2', 'USER', NOW(), NOW()),
    ('creator@test.com', '$2a$10$8K1p/a/zPYp5.L8UG8pXOuLqE5LHG8TGY4YoFJ2mDVLHaKL9hTWmG', '질문제작자', 'USER', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- 2. 테스트 질문들 생성 (자동 승인됨)
INSERT INTO question (text, option_a_text, option_b_text, keyword, is_active, approval_status, creator_id, created_at, updated_at)
VALUES 
    ('커피 vs 차, 어떤 것을 더 좋아하시나요?', '커피', '차', '음료', true, 'APPROVED', 3, NOW(), NOW()),
    ('바다 vs 산, 어디서 휴가를 보내고 싶나요?', '바다', '산', '여행', true, 'APPROVED', 3, NOW(), NOW()),
    ('아침형 인간 vs 밤형 인간, 당신은?', '아침형 인간', '밤형 인간', '라이프스타일', true, 'APPROVED', 3, NOW(), NOW()),
    ('고양이 vs 강아지, 어떤 반려동물을 선호하나요?', '고양이', '강아지', '반려동물', true, 'APPROVED', 3, NOW(), NOW()),
    ('영화관 vs 집, 영화는 어디서 보는 게 좋나요?', '영화관', '집', '엔터테인먼트', true, 'APPROVED', 3, NOW(), NOW()),
    ('피자 vs 치킨, 야식으로 뭘 시킬까요?', '피자', '치킨', '음식', true, 'APPROVED', 3, NOW(), NOW()),
    ('책 vs 전자책, 어떤 방식으로 읽나요?', '종이책', '전자책', '독서', true, 'APPROVED', 3, NOW(), NOW()),
    ('여름 vs 겨울, 어떤 계절을 더 좋아하나요?', '여름', '겨울', '계절', true, 'APPROVED', 3, NOW(), NOW()),
    ('현금 vs 카드, 어떤 결제 방식을 선호하나요?', '현금', '카드', '결제', true, 'APPROVED', 3, NOW(), NOW()),
    ('라면 vs 밥, 간단한 식사로 뭘 선택하나요?', '라면', '밥', '간식', true, 'APPROVED', 3, NOW(), NOW());

-- 3. 공개 질문 번들 생성
INSERT INTO question_bundle (title, description, keywords, is_public, creator_id, created_at, updated_at)
VALUES 
    ('일상 선택의 순간들', '우리가 매일 마주치는 소소한 선택들에 대한 밸런스 게임', '일상,라이프스타일,선택', true, 3, NOW(), NOW()),
    ('음식 vs 음식', '맛있는 음식들 사이의 달콤한 고민', '음식,요리,맛', true, 3, NOW(), NOW()),
    ('취미와 여가 시간', '자유시간을 보내는 다양한 방법들', '취미,여가,엔터테인먼트', true, 3, NOW(), NOW());

-- 4. 번들과 질문 연결
INSERT INTO bundle_question (bundle_id, question_id, order_index)
VALUES 
    -- 일상 선택의 순간들 번들
    (1, 1, 1), -- 커피 vs 차
    (1, 3, 2), -- 아침형 vs 밤형
    (1, 4, 3), -- 고양이 vs 강아지
    (1, 9, 4), -- 현금 vs 카드
    
    -- 음식 vs 음식 번들
    (2, 6, 1), -- 피자 vs 치킨
    (2, 10, 2), -- 라면 vs 밥
    (2, 1, 3), -- 커피 vs 차
    
    -- 취미와 여가 시간 번들
    (3, 2, 1), -- 바다 vs 산
    (3, 5, 2), -- 영화관 vs 집
    (3, 7, 3), -- 책 vs 전자책
    (3, 8, 4); -- 여름 vs 겨울

-- 5. 질문별 통계 초기화
INSERT INTO question_stats (question_id, option_a_count, option_b_count, total_count, updated_at)
VALUES 
    (1, 45, 55, 100, NOW()),
    (2, 62, 38, 100, NOW()),
    (3, 34, 66, 100, NOW()),
    (4, 48, 52, 100, NOW()),
    (5, 72, 28, 100, NOW()),
    (6, 58, 42, 100, NOW()),
    (7, 39, 61, 100, NOW()),
    (8, 44, 56, 100, NOW()),
    (9, 23, 77, 100, NOW()),
    (10, 67, 33, 100, NOW());

-- 6. 번들별 통계 초기화
INSERT INTO question_bundle_stats (bundle_id, play_count)
VALUES 
    (1, 45),
    (2, 32),
    (3, 28);

-- 7. 샘플 게임 세션 생성
INSERT INTO game_session (bundle_id, player_user_id, created_at, session_status, completed_at)
VALUES 
    (1, 1, NOW() - INTERVAL '2 days', 'COMPLETED', NOW() - INTERVAL '2 days' + INTERVAL '5 minutes'),
    (2, 2, NOW() - INTERVAL '1 day', 'COMPLETED', NOW() - INTERVAL '1 day' + INTERVAL '3 minutes'),
    (3, 1, NOW() - INTERVAL '3 hours', 'IN_PROGRESS', NULL);

-- 8. 샘플 사용자 답변
INSERT INTO user_answer (question_id, user_id, session_id, selected_option, answered_at)
VALUES 
    -- 첫 번째 세션 (완료됨)
    (1, 1, 1, 'A', NOW() - INTERVAL '2 days'),
    (3, 1, 1, 'B', NOW() - INTERVAL '2 days' + INTERVAL '1 minute'),
    (4, 1, 1, 'A', NOW() - INTERVAL '2 days' + INTERVAL '2 minutes'),
    (9, 1, 1, 'B', NOW() - INTERVAL '2 days' + INTERVAL '3 minutes'),
    
    -- 두 번째 세션 (완료됨)
    (6, 2, 2, 'A', NOW() - INTERVAL '1 day'),
    (10, 2, 2, 'A', NOW() - INTERVAL '1 day' + INTERVAL '1 minute'),
    (1, 2, 2, 'B', NOW() - INTERVAL '1 day' + INTERVAL '2 minutes');

-- 성공 메시지
DO $$
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE '테스트 데이터 삽입이 완료되었습니다!';
    RAISE NOTICE '==========================================';
    RAISE NOTICE '생성된 데이터:';
    RAISE NOTICE '- 테스트 사용자: 3명';
    RAISE NOTICE '- 승인된 질문: 10개';
    RAISE NOTICE '- 공개 질문 번들: 3개';
    RAISE NOTICE '- 게임 세션: 3개';
    RAISE NOTICE '- 사용자 답변: 7개';
    RAISE NOTICE '==========================================';
END $$;