package com.example.chuong.librarymanagementapi.exception;

import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
            MethodArgumentNotValidException exception
    ){
        Map<String,String> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400,"Validation failed",errors));
    }

    /**
     * Bắt lỗi khi client truyền sort field không tồn tại trên Entity
     * (ví dụ sort=abcXyz,asc mà entity không có field "abcXyz").
     * Đây là lỗi Spring Data JPA ném ra ở tầng dưới, nằm ngoài whitelist check
     * của PaginationUtils (phòng trường hợp field có trong whitelist nhưng
     * bị đổi tên/entity thay đổi mà quên cập nhật whitelist).
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Void>> handlePropertyReferenceException(
            PropertyReferenceException exception
    ){
        log.warn("Invalid sort/property reference: {}",exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCode.INVALID_SORT_FIELD.getCode())
                .message(ErrorCode.INVALID_SORT_FIELD.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    /**
     * Bắt lỗi khi client truyền sai kiểu dữ liệu cho tham số query,
     * điển hình nhất là page/size không phải số nguyên (?page=abc).
     * Spring sẽ tự ném MethodArgumentTypeMismatchException trước khi
     * vào tới Controller, nên phải bắt riêng ở đây.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        log.warn("Type mismatch for parameter '{}' : value = '{}' ,requiredType = {}",
        exception.getName(),
        exception.getValue(),
        exception.getRequiredType());

        String detailsMessage = String.format(
                "%s: tham số '%s' phải có kiểu %s, nhưng nhận giá trị '%s'",
                ErrorCode.INVALID_PAGINATION_PARAMS.getMessage(),
                exception.getName(),
                exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "hợp lệ",
                exception.getValue()
        );

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCode.INVALID_PAGINATION_PARAMS.getCode())
                .message(detailsMessage)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }

    /**
     * Bắt IllegalArgumentException chung, bao gồm cả trường hợp
     * validate thủ công minPrice > maxPrice trong Service layer
     * (nếu bạn chọn ném IllegalArgumentException thay vì AppException ở đó).
     *
     * Lưu ý: nếu BookService đã ném AppException(INVALID_PRICE_RANGE) như
     * trong plan Bước 3, thì case đó đã được xử lý bởi handler AppException
     * có sẵn của bạn, không rơi vào đây. Handler này chỉ là lưới an toàn
     * (safety net) cho các IllegalArgumentException phát sinh ngoài dự kiến.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException exception
    ){
        log.warn("Illegal argument: {}",exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCode.INVALID_PAGINATION_PARAMS.getCode())
                .message(exception.getMessage() != null
                ? exception.getMessage()
                        :ErrorCode.INVALID_PAGINATION_PARAMS.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // AppException
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(
            AppException exception
    ){
        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(exception.getMessage())
                .result(null)
                .build();
        return ResponseEntity.status(errorCode.getCode())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception exception){
        ApiResponse<?> error = ApiResponse.builder()
                .code(500).message("Internal server error").result(null).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
