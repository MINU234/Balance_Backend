-- V6: keywords 컬럼이 누락된 경우 추가
-- 안전하게 컬럼이 존재하지 않는 경우에만 추가

DO $$
BEGIN
    -- keywords 컬럼이 존재하지 않는 경우에만 추가
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'question_bundle' 
        AND column_name = 'keywords'
        AND table_schema = 'public'
    ) THEN
        ALTER TABLE public.question_bundle 
        ADD COLUMN keywords VARCHAR(255);
        
        RAISE NOTICE 'keywords 컬럼이 question_bundle 테이블에 추가되었습니다.';
    ELSE
        RAISE NOTICE 'keywords 컬럼이 이미 존재합니다.';
    END IF;
END $$;