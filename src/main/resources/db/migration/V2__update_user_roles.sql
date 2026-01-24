-- 기존 사용자들의 role을 USER로 설정
UPDATE "user" SET role = 'USER' WHERE role IS NULL;

-- role 컬럼을 NOT NULL로 변경 (이미 설정되어 있지만 확실히)
ALTER TABLE "user" ALTER COLUMN role SET NOT NULL;