package com.example.chuong.librarymanagementapi.mapper;

import com.example.chuong.librarymanagementapi.dto.response.Borrow.BorrowResponse;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "overdueDays", ignore = true)
    @Mapping(target = "fineAmount", ignore = true)
    BorrowResponse toResponse(Borrow borrow);
}
