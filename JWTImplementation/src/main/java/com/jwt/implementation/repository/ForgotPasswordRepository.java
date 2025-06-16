package com.jwt.implementation.repository;

import com.jwt.implementation.entity.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {

    @Query("SELECT fp FROM ForgotPassword fp JOIN fp.user u WHERE fp.otp = :otp AND u.email = :email")
    Optional<ForgotPassword> findByOtpAndUserEmail(@Param("otp") Integer otp, @Param("email") String email);

    @Modifying
    @Query("DELETE FROM ForgotPassword fp WHERE fp.user.id = :userId")
    int deleteByUserId(@Param("userId") Integer userId);
}