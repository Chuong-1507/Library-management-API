package com.example.chuong.librarymanagementapi.config;

import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.BookResponse;
import com.example.chuong.librarymanagementapi.dto.response.CategoryListResponse;
import com.example.chuong.librarymanagementapi.dto.response.CategoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class RedisSerializationTest {

    @Test
    void testArraySerialization() {
        RedisSerializer<Object> serializer = RedisSerializer.json();

        CategoryResponse category = CategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Fiction")
                .build();
        CategoryResponse[] array = new CategoryResponse[]{category};

        byte[] bytes = serializer.serialize(array);
        System.out.println("Array Serialized: " + new String(bytes));
        Object deserialized = serializer.deserialize(bytes);
        System.out.println("Array Deserialized: " + deserialized);
    }

    @Test
    void testCategoryListResponseSerialization() {
        RedisSerializer<Object> serializer = RedisSerializer.json();

        CategoryResponse category = CategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Fiction")
                .build();
        CategoryListResponse response = CategoryListResponse.of(List.of(category));

        byte[] bytes = serializer.serialize(response);
        System.out.println("CategoryListResponse Serialized: " + new String(bytes));
        Object deserialized = serializer.deserialize(bytes);
        System.out.println("CategoryListResponse Deserialized: " + deserialized);

        assertInstanceOf(CategoryListResponse.class, deserialized);
        CategoryListResponse listResponse = (CategoryListResponse) deserialized;
        assertEquals("Fiction", listResponse.getCategories().get(0).getName());
    }
}

