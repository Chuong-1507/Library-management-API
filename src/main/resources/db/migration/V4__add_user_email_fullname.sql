ALTER TABLE users ADD COLUMN email VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Tuỳ chọn: backfill cho tài khoản admin seed sẵn trước migration này
-- (đổi 'admin' và giá trị cho khớp AdminSeeder thật của bạn)
-- UPDATE users SET email = 'admin@library.local', full_name = 'System Administrator'
-- WHERE username = 'admin' AND email IS NULL;