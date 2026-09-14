package com.example.chuong.librarymanagementapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   
public class LibraryManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApiApplication.class, args);
    }

}
