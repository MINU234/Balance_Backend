ALTER TABLE public.user_answer
    ADD COLUMN session_id bigint;

-- 2) 외래키 제약조건 추가
ALTER TABLE public.user_answer
    ADD CONSTRAINT fk_answer_session
        FOREIGN KEY (session_id) REFERENCES public.game_session(session_id);

-- 3) 인덱스 추가 (검색용)
CREATE INDEX idx_user_answer_session_id ON public.user_answer(session_id);
