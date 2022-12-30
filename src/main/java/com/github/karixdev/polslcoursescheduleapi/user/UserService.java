package com.github.karixdev.polslcoursescheduleapi.user;

import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.exception.EmailNotAvailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private boolean isEmailAvailable(String email) {
        return repository.findByEmail(email).isEmpty();
    }

    @Transactional
    public User createUser(String email, String plainPassword, UserRole userRole, Boolean isEnabled) {
        if (!isEmailAvailable(email)) {
            throw new EmailNotAvailableException();
        }

        String encodedPassword = passwordEncoder.encode(plainPassword);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .isEnabled(isEnabled)
                .userRole(userRole)
                .build();

        user = repository.save(user);

        return user;
    }

    @Transactional
    public void enableUser(User user) {
        user.setIsEnabled(Boolean.TRUE);
        repository.save(user);
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException(
                            "User with provided email not found");
                });
    }
}
