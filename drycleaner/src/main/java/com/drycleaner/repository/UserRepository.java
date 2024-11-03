package com.drycleaner.repository;

import com.drycleaner.Model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional findByEmail(String email);
    Page<UserModel> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

}
