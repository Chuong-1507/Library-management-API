package com.example.chuong.librarymanagementapi.service.serviceImpl;

import com.example.chuong.librarymanagementapi.config.PaginationUtils;
import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.BorrowResponse;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.BorrowMapper;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.BorrowRepository;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.service.BorrowService;
import com.example.chuong.librarymanagementapi.service.EmailService;
import com.example.chuong.librarymanagementapi.specification.BorrowSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowMapper borrowMapper;
    private final EmailService emailService;

    private static final int MAX_ACTIVE_BORROWS = 5;
    private static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(5000); // 5,000 VND / ngày quá hạn

    //WhiteList sort field của Borrow
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id","borrowDate","dueDate","returnDate","status","fineAmount","createdAt"
    );
    private static final String DEFAULT_SORT_FIELD = "borrowDate";


    /**
     * Mục tiêu: Xử lý quy trình mượn sách mới của người dùng.
     * 
     * Cách thức hoạt động:
     * 1. Xác thực người dùng qua username từ JWT token context.
     * 2. Validate thời gian mượn/trả (ngày mượn không thể ở quá khứ, ngày trả không trước ngày mượn).
     * 3. Kiểm tra số sách đang mượn của user (BORROWING + OVERDUE) <= MAX_ACTIVE_BORROWS (5 cuốn).
     * 4. Kiểm tra user có đang mượn cuốn sách này mà chưa trả hay không.
     * 5. Khóa dòng sách bằng Pessimistic Write Lock (findByIdWithLock) để chống race condition concurrent checkout.
     * 6. Giảm số lượng tồn kho của sách đi 1 và lưu thay đổi vào DB.
     * 7. Tạo và khởi tạo phiếu mượn mới trạng thái BORROWING, lưu DB và enrich thông tin trả về.
     */
    @CacheEvict(value = "books", allEntries = true) // Sau khi mượn sách thành công, xóa cache books vì số lượng sách đã thay đổi.
    @Override
    @Transactional
    public BorrowResponse createBorrow(String username, BorrowCreateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate ngày mượn không ở quá khứ
        if (request.getBorrowDate() != null && request.getBorrowDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_RETURN_DATE);
        }

        if (request.getReturnDate() == null || (request.getBorrowDate() != null && request.getReturnDate().isBefore(request.getBorrowDate()))) {
            throw new AppException(ErrorCode.INVALID_RETURN_DATE);
        }

        // Đếm tổng số sách đang mượn (BORROWING và OVERDUE)
        long activeBorrows = borrowRepository.countByUserIdAndStatusIn(
                user.getId(), List.of(Status.BORROWING, Status.OVERDUE));
        if (activeBorrows >= MAX_ACTIVE_BORROWS) {
            throw new AppException(ErrorCode.BORROW_LIMIT_EXCEEDED);
        }

        // Kiểm tra xem user có đang mượn cùng cuốn sách này mà chưa trả hay không
        boolean duplicateBorrow = borrowRepository.existsByUserIdAndBookIdAndStatusIn(
                user.getId(), request.getBookId(), List.of(Status.BORROWING, Status.OVERDUE));
        if (duplicateBorrow) {
            throw new AppException(ErrorCode.ALREADY_BORROWED);
        }

        // Khóa pessimistic lock khi đọc sách để trừ tồn kho an toàn
        Book book = bookRepository.findByIdWithLock(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
            throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
        }
        // Trừ số lượng sách tồn kho
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        // Tạo giao dịch mượn
        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(request.getBorrowDate());
        borrow.setReturnDate(request.getReturnDate());
        borrow.setStatus(Status.BORROWING);

        Borrow savedBorrow = borrowRepository.save(borrow);
        emailService.sendBorrowConfirmation(user,book,borrow.getReturnDate());

        return enrichResponse(borrowMapper.toResponse(savedBorrow), savedBorrow);
    }

    /**
     * Mục tiêu: Xử lý thủ tục trả sách và tính toán tiền phạt quá hạn.
     * 
     * Cách thức hoạt động:
     * 1. Tìm phiếu mượn theo ID.
     * 2. Phân quyền: Kiểm tra người dùng hiện tại có phải chủ phiếu mượn hoặc có quyền ADMIN không.
     * 3. Ghi nhận ngày trả thực tế là ngày hiện tại (LocalDate.now()).
     * 4. Tính toán tiền phạt: Số ngày quá hạn * FINE_PER_DAY (5.000 VNĐ/ngày).
     * 5. Đổi trạng thái phiếu mượn thành RETURNED.
     * 6. Khóa ghi (Pessimistic Lock) đầu sách và tăng số lượng tồn kho thêm 1.
     * 7. Lưu lại thông tin phiếu mượn và sách vào DB.
     */
    @CacheEvict(value = "books", allEntries = true) // Sau khi trả sách thành công, xóa cache books vì số lượng sách đã thay đổi.
    @Override
    @Transactional
    public BorrowResponse returnBook(UUID borrowId, String currentUsername, boolean isAdmin) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_FOUND));

        if (!isAdmin && !borrow.getUser().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.USER_FALSE);
        }

        if (borrow.getStatus() == Status.RETURNED) {
            throw new AppException(ErrorCode.ALREADY_RETURNED);
        }

        LocalDate today = LocalDate.now();
        borrow.setActualReturnDate(today);

        // Tính tiền phạt nếu trả quá hạn
        if (today.isAfter(borrow.getReturnDate())) {
            long overdueDays = ChronoUnit.DAYS.between(borrow.getReturnDate(), today);
            borrow.setFineAmount(FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays)));
        } else {
            borrow.setFineAmount(BigDecimal.ZERO);
        }

        // Cập nhật trạng thái mượn
        borrow.setStatus(Status.RETURNED);

        // Cộng trả lại số lượng tồn kho với Pessimistic Lock
        if (borrow.getBook() != null) {
            Book book = bookRepository.findByIdWithLock(borrow.getBook().getId())
                    .orElse(borrow.getBook());
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            bookRepository.save(book);
        }

        Borrow updatedBorrow = borrowRepository.save(borrow);
        if (borrow.getFineAmount() != null && borrow.getFineAmount().compareTo(BigDecimal.ZERO) > 0){
            emailService.sendFineNotification(borrow.getUser(),borrow.getBook(),borrow.getFineAmount());
        }

        return enrichResponse(borrowMapper.toResponse(updatedBorrow), updatedBorrow);
    }


    //Phân trang
    @Override
    public PageResponse<BorrowResponse> getMyBorrows(Authentication authentication, BorrowFilterRequest filterRequest, int page, int size, String sort) {
        // ===== ĐIỂM BẢO MẬT QUAN TRỌNG NHẤT =====
        // Lấy username TRỰC TIẾP từ Authentication (JWT principal), không đọc
        // từ bất kỳ tham số nào client gửi lên. Dù filter.getSearchUser() có
        // bị client set giá trị khác, nó VẪN bị bỏ qua vì
        // BorrowSpecification.withFilterForCurrentUser() không đọc field đó.
        String currentUsername = authentication.getName();

        if (filterRequest.isDateRangeInvalid()){
            throw new IllegalArgumentException("fromDate không thể sau toDate");
        }
        Pageable pageable = PaginationUtils.createPageable(
                page,size,sort,ALLOWED_SORT_FIELDS,DEFAULT_SORT_FIELD
        );

        var spec = BorrowSpecification.withFilterForCurrentUser(filterRequest,currentUsername);
        Page<Borrow> borrowPage = borrowRepository.findAll(spec,pageable);
        Page<BorrowResponse> responses = borrowPage.map(borrowMapper::toResponse);

        int normalizePage = PaginationUtils.normalizePage(page);
        return PageResponse.fromPage(responses,normalizePage);
    }

    //Phân trang
    @Override
    public PageResponse<BorrowResponse> getAllBorrows(BorrowFilterRequest filterRequest, int page, int size, String sort) {
        // Admin API: được phép lọc theo filter.getSearchUser() do chính
        // endpoint này chỉ mở cho role ADMIN (chặn ở Controller/Security Config).
        if (filterRequest.isDateRangeInvalid()){
            throw new IllegalArgumentException("fromDate không thể sau toDate");
        }
        Pageable pageable = PaginationUtils.createPageable(
                page,size,sort,ALLOWED_SORT_FIELDS,DEFAULT_SORT_FIELD
        );

        var spec = BorrowSpecification.withFilterForAdmin(filterRequest);
        Page<Borrow> borrows = borrowRepository.findAll(spec,pageable);
        Page<BorrowResponse> responsePage = borrows.map(borrowMapper::toResponse);

        int normalizedPage = PaginationUtils.normalizePage(page);
        return PageResponse.fromPage(responsePage,normalizedPage);
    }

    /**
     * Mục tiêu: Chạy tác vụ quét và cập nhật trạng thái phiếu mượn quá hạn trong cơ sở dữ liệu.
     * 
     * Cách thức hoạt động:
     * - Thực thi trực tiếp câu lệnh SQL UPDATE tại repository để đổi trạng thái BORROWING -> OVERDUE 
     *   đối với các phiếu mượn có ngày trả dự kiến (returnDate) nhỏ hơn ngày hiện tại (LocalDate.now()).
     */
    @Override
    @Transactional
    public int updateOverdueBorrows() {
        return borrowRepository.updateOverdueStatus(Status.BORROWING, Status.OVERDUE, LocalDate.now());
    }



    /**
     * Mục tiêu: Bổ sung các thông tin tính toán động (phí phạt, số ngày quá hạn, hiển thị trạng thái OVERDUE tạm thời) vào DTO phản hồi API.
     * 
     * Cách thức hoạt động:
     * 1. Nếu phiếu đã trả (RETURNED): Tính số ngày trễ giữa actualReturnDate và returnDate.
     * 2. Nếu phiếu chưa trả (BORROWING/OVERDUE): So sánh returnDate với ngày hiện tại (LocalDate.now()).
     *    Nếu trễ hạn, gán status tạm thời thành OVERDUE trong response, đồng thời tính overdueDays và fineAmount tương ứng.
     */
    private BorrowResponse enrichResponse(BorrowResponse response, Borrow borrow) {
        LocalDate today = LocalDate.now();
        if (borrow.getStatus() == Status.RETURNED) {
            response.setActualReturnDate(borrow.getActualReturnDate());
            LocalDate actual = borrow.getActualReturnDate() != null ? borrow.getActualReturnDate() : borrow.getReturnDate();
            if (actual.isAfter(borrow.getReturnDate())) {
                long overdueDays = ChronoUnit.DAYS.between(borrow.getReturnDate(), actual);
                response.setOverdueDays(overdueDays);
                BigDecimal fine = borrow.getFineAmount() != null
                        ? borrow.getFineAmount()
                        : FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
                response.setFineAmount(fine);
            } else {
                response.setOverdueDays(0);
                response.setFineAmount(BigDecimal.ZERO);
            }
        } else {
            // Đang mượn (BORROWING hoặc OVERDUE)
            if (today.isAfter(borrow.getReturnDate())) {
                long overdueDays = ChronoUnit.DAYS.between(borrow.getReturnDate(), today);
                response.setOverdueDays(overdueDays);
                response.setFineAmount(FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays)));
                response.setStatus(Status.OVERDUE);
            } else {
                response.setOverdueDays(0);
                response.setFineAmount(BigDecimal.ZERO);
            }
        }
        return response;
    }
}
