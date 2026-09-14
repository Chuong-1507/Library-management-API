CREATE TABLE `category` (
                            `id` binary(16) NOT NULL,
                            `name` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UK46ccwnsi9409t36lurvtyljak` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
                         `id` binary(16) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `username` varchar(255) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `books` (
  `created_at` date DEFAULT NULL,
  `price` decimal(38,2) NOT NULL,
  `publication_year` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `category_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `author` varchar(255) NOT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5mtto2jcmfrwfg0p1ui8mnweu` (`title`),
  KEY `idx_book_title` (`title`),
  KEY `idx_book_author` (`author`),
  KEY `idx_book_category_id` (`category_id`),
  KEY `idx_book_price` (`price`),
  CONSTRAINT `FK8el3ddb59ciucupyc17vu7835` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `books_chk_1` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `borrows` (
  `actual_return_date` date DEFAULT NULL,
  `borrow_date` date NOT NULL,
  `fine_amount` double DEFAULT NULL,
  `return_date` date NOT NULL,
  `book_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `status` enum('BORROWING','OVERDUE','RETURNED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_borrow_user_id` (`user_id`),
  KEY `idx_borrow_book_id` (`book_id`),
  KEY `idx_borrow_status` (`status`),
  KEY `idx_borrow_date` (`borrow_date`),
  CONSTRAINT `FK8789wjikihu9ocbhamiw789y9` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  CONSTRAINT `FKd64ttskt7j96v1nqtpry3pp2a` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;




CREATE TABLE `user_roles` (
  `user_id` binary(16) NOT NULL,
  `role` enum('ADMIN','USER') DEFAULT NULL,
  UNIQUE KEY `UKkpjrhqbpn9eqscssk9ttp7glu` (`user_id`,`role`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;




