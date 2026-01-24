CREATE INDEX idx_question_keyword ON public.question(keyword) WHERE keyword IS NOT NULL;
CREATE INDEX idx_question_is_active ON public.question(is_active);
CREATE INDEX idx_question_keyword_active ON public.question(keyword, is_active)
    WHERE keyword IS NOT NULL AND is_active = true;

-- question_bundle 테이블: 제목, 설명
CREATE INDEX idx_qb_title ON public.question_bundle USING gin(to_tsvector('english', title));
CREATE INDEX idx_qb_description ON public.question_bundle USING gin(to_tsvector('english', description));
CREATE INDEX idx_qb_search ON public.question_bundle(title, description);

-- user 테이블: 최근 활동 사용자 조회
CREATE INDEX idx_user_updated_at ON public."user"(updated_at DESC);

-- question_stats 테이블: 최신 통계 조회
CREATE INDEX idx_qstats_updated_at ON public.question_stats(updated_at DESC);

-- bundle_question 테이블: 순서 정렬
CREATE INDEX idx_bundle_question_order ON public.bundle_question(bundle_id, order_index);
