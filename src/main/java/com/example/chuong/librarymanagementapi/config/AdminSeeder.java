package com.example.chuong.librarymanagementapi.config;

import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String @NonNull ... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()){
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(Role.ADMIN,Role.USER));
            userRepository.save(admin);
            System.out.println("ADMIN has been created with password: 123456, please change it ! ");

        }

    }
}

//private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String @NonNull ... args) throws Exception {
//        if (userRepository.findByUsername("admin").isEmpty()){
//            User admin = new User();
//            admin.setUsername("admin");
//            admin.setPassword(passwordEncoder.encode("123456"));
//            admin.setRoles(Set.of(Role.ADMIN,Role.USER));//admin vừa là admin, vừa là user
//            userRepository.save(admin);
//            System.out.println("ADMIN has been created with password: 123456, please change it !");
//        }
//    }