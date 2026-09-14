package com.example.chuong.librarymanagementapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(unique = true,nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    @JsonIgnoreProperties("category")// khi serialize books, bỏ qua category của từng book (tránh vòng lặp)
    @Builder.Default
    private List<Book> books = new ArrayList<>();

}
