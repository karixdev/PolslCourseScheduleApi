package com.github.karixdev.polslcoursescheduleapi.user;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_email_unique",
                        columnNames = "email"
                )
        }
)
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_gen"
    )
    @SequenceGenerator(
            name = "user_gen",
            sequenceName = "user_seq",
            allocationSize = 1
    )
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(
            name = "email",
            nullable = false
    )
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "user_role",
            nullable = false
    )
    private UserRole userRole;

    @Column(
            name = "password",
            nullable = false
    )
    private String password;

    @Column(
            name = "is_enabled",
            nullable = false
    )
    private Boolean isEnabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                userRole == user.userRole &&
                Objects.equals(password, user.password) &&
                Objects.equals(isEnabled, user.isEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, userRole, password, isEnabled);
    }
}
