package com.inventra.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventra.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    User findByResetToken(String resetToken);
}
