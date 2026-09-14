package com.example.chuong.librarymanagementapi.specification;

import ch.qos.logback.core.util.StringUtil;
import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowFilterRequest;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import jakarta.validation.constraints.Null;
import lombok.experimental.UtilityClass;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@UtilityClass
public class BorrowSpecification {
    public Specification<Borrow> hasUsername(String username){
        return ((root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(username)){
                return criteriaBuilder.conjunction();
            }
            // Giả định Borrow -> User có quan hệ "user" với field "username"
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("user").get("username")), username.toLowerCase());
        });
    }

    public static Specification<Borrow> hasStatus(Status status){
        return ((root, query, criteriaBuilder) -> {
            if (status == null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"),status);
        });
    }

    public static Specification<Borrow> borrowDateBetween(LocalDate fromDate, LocalDate toDate){
        return ((root, query, criteriaBuilder) -> {
            if (fromDate != null && toDate != null){
                return criteriaBuilder.between(root.get("borrowDate"),fromDate,toDate);
            }
            if (fromDate != null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("borrowDate"), fromDate);
            }
            if (toDate != null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("borrowDate"),toDate);
            }
            return criteriaBuilder.conjunction();
        });
    }
    /**
     * Dùng cho Admin API (GET /api/borrows) — cho phép lọc theo searchUser từ filter.
     */
    public static Specification<Borrow> withFilterForAdmin(BorrowFilterRequest filter){
        return Specification.where(hasUsername(filter.getSearchUser()))
                .and(hasStatus(filter.getStatus()))
                .and(borrowDateBetween(filter.getFromDate(),filter.getToDate()));
    }
    /**
     * Dùng cho /my-borrows — username được truyền RIÊNG từ SecurityContext
     * (tham số currentUsername), KHÔNG lấy từ filter.getSearchUser() để
     * đảm bảo client không thể ghi đè và xem dữ liệu của người khác (chống IDOR).
     */
    public static Specification<Borrow> withFilterForCurrentUser(
            BorrowFilterRequest filter,
            String currentUsername){
        return Specification.where(hasUsername(currentUsername))
                .and(hasStatus(filter.getStatus()))
                .and(borrowDateBetween(filter.getFromDate(),filter.getFromDate()));
    }
}
