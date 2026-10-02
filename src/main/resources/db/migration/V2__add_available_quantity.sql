-- V2__add_availavle_quantity.sql
ALTER TABLE books ADD COLUMN total_quantity INT NOT NULL DEFAULT 0;
ALTER TABLE books ADD COLUMN available_quantity INT NOT NULL DEFAULT 0;

-- available = quantity hiện tại (phản ánh đúng số sách có thể mượn được ngay tại thời điểm)
UPDATE books SET available_quantity = quantity;

-- total = available + quantity( số lượng sách đang đc mượn - chưa trả)
UPDATE books b
SET total_quantity = b.quantity + (
    SELECT COUNT(*) FROM borrows br
                    WHERE br.book_id = b.id
                    AND br.actual_return_date IS NULL
    );

ALTER TABLE books DROP COLUMN quantity;