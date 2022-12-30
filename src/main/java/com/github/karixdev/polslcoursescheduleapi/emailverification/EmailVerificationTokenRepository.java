package com.github.karixdev.polslcoursescheduleapi.emailverification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    @Query("""
            SELECT eToken
            FROM EmailVerificationToken eToken
            WHERE eToken.token = :token
            """)
    Optional<EmailVerificationToken> findByToken(@Param("token") String token);
}
