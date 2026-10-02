package com.example.chuong.librarymanagementapi.mapper;

import com.example.chuong.librarymanagementapi.dto.request.BookCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.response.BookResponse;
import com.example.chuong.librarymanagementapi.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;


@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableQuantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Book createToBook(BookCreateRequest request);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableQuantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateBook(BookUpdateRequest request,
                    @MappingTarget Book book
    );

    @Mapping(target = "categoryName",source = "category.name")
    BookResponse toResponse(Book book);
}

