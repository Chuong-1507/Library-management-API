package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User,UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
