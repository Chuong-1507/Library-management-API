package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.Borrow.BorrowResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface BorrowService {
    /**
     * Mục tiêu: Xử lý tạo mới một phiếu mượn sách cho người dùng.
     * Cách thức: Kiểm tra thông tin user, ràng buộc mượn tối đa (5 cuốn), kiểm tra mượn trùng sách,
     * thực hiện Pessimistic Lock trên đầu sách để trừ tồn kho an toàn và lưu phiếu mượn với trạng thái BORROWING.
     */
    BorrowResponse createBorrow(String username, BorrowCreateRequest request);

    /**
     * Mục tiêu: Xử lý quy trình trả sách của người dùng hoặc do Admin thực hiện giúp.
     * Cách thức: Kiểm tra quyền sở hữu phiếu mượn/quyền Admin, cập nhật ngày trả thực tế, 
     * tính toán phí phạt quá hạn (5,000 VND/ngày nếu quá hạn), cập nhật trạng thái RETURNED và cộng trả số lượng sách tồn kho (dùng Lock).
     */
    BorrowResponse returnBook(UUID borrowId, String currentUsername, boolean isAdmin);

    /**
     * Mục tiêu: Lấy danh sách phiếu mượn sách của người dùng đang đăng nhập.
     * Cách thức: Tìm tất cả phiếu mượn thuộc về User (có thể lọc theo Status như BORROWING, OVERDUE, RETURNED),
     * sau đó bổ sung thông tin số ngày quá hạn và tiền phạt hiện tại trước khi trả về.
     */
    List<BorrowResponse> getMyBorrows(String username, Status status);

    /**
     * Mục tiêu: Lấy toàn bộ phiếu mượn sách trong hệ thống dành cho Quản trị viên (ADMIN).
     * Cách thức: Truy vấn tất cả phiếu mượn trong cơ sở dữ liệu (tối ưu hóa fetch join bằng EntityGraph),
     * hỗ trợ lọc theo trạng thái và tính toán động phí phạt/số ngày quá hạn.
     */
    List<BorrowResponse> getAllBorrows(Status status);

    /**
     * Mục tiêu: Cập nhật batch hàng loạt các phiếu mượn quá hạn sang trạng thái OVERDUE.
     * Cách thức: Gọi phương thức repository thực thi câu lệnh SQL UPDATE trực tiếp với điều kiện returnDate < today và status = BORROWING.
     */
    int updateOverdueBorrows();

    /**
     * Lấy danh sách phiếu mượn của CHÍNH người dùng đang đăng nhập.
     * username được trích xuất từ Authentication ở tầng Service,
     * KHÔNG bao giờ nhận username/userId từ filter do client gửi.
     */
    PageResponse<BorrowResponse> getMyBorrows(
            Authentication authentication,
            BorrowFilterRequest filterRequest,
            int page,
            int size,
            String sort
    );

    /**
     * Admin API: lấy toàn bộ phiếu mượn, có thể lọc theo searchUser.
     */
    PageResponse<BorrowResponse> getAllBorrows(
            BorrowFilterRequest filterRequest,
            int page,
            int size,
            String sort
    );
}
