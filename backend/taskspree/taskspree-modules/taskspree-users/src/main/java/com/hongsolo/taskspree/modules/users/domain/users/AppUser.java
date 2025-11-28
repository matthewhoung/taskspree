package com.hongsolo.taskspree.modules.users.domain.users;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users", schema = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

    @Column(name = "identity_id", nullable = false, unique = true)
    private UUID identityId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "user_name", nullable = false, length = 50)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private AppUser(UUID identityId, String email, String username) {
        this.identityId = identityId;
        this.email = email;
        this.username = username;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static AppUser create(UUID identityId, String email) {
        String username = email.split("@")[0];
        return new AppUser(identityId, email, username);
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
        this.updatedAt = Instant.now();
    }
}