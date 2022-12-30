package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    @Query("""
            SELECT eToken
            FROM EmailVerificationToken eToken
            WHERE eToken.token = :token
            """)
    Optional<EmailVerificationToken> findByToken(@Param("token") String token);

    @Query("""
            SELECT eToken
            FROM EmailVerificationToken eToken
            WHERE eToken.user = :user
            ORDER BY eToken.createdAt DESC
            """)
    List<EmailVerificationToken> findByUserOrderByCreatedAtDesc(@Param("user") User user);
}
