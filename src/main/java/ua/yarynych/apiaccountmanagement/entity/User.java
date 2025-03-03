package ua.yarynych.apiaccountmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.RegistrationRequest;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    @JsonIgnore
    private String password;

    @Column
    @JsonIgnore
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Builder.Default
    private Date createdAt = new Date();

    public static User build(RegistrationRequest request) {
        return User.builder()
                .firstName(Optional.ofNullable(request.getFirstName())
                        .orElseThrow(() -> new IllegalArgumentException("First name is required")))
                .secondName(Optional.ofNullable(request.getSecondName())
                        .orElseThrow(() -> new IllegalArgumentException("Second name is required")))
                .phone(Optional.ofNullable(request.getPhone())
                        .orElseThrow(() -> new IllegalArgumentException("Phone is required")))
                .email(Optional.ofNullable(request.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Email is required")))
                .password(Optional.ofNullable(request.getPassword())
                        .orElseThrow(() -> new IllegalArgumentException("Password is required")))
                .role(Optional.ofNullable(request.getRole())
                        .orElseThrow(() -> new IllegalArgumentException("Role is required")))
                .createdAt(new Date())
                .build();
    }

    @Override
    public String toString() {
        return String.format("[%s, %s (%s)] %s %s",
                email,
                role == null ? "UNKNOWN_ROLE" : role,
                id,
                firstName == null ? "UNKNOWN_NAME" : firstName,
                secondName == null ? "UNKNOWN_SURNAME" : secondName
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id) && email.equals(user.email) && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, role);
    }
}
