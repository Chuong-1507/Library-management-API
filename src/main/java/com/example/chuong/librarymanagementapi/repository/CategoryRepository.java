package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.Category;
import jakarta.validation.constraints.NotNull;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category,UUID>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "books")
    @Query("select c from Category c")
    List<Category> findAllWithBooks();

}
