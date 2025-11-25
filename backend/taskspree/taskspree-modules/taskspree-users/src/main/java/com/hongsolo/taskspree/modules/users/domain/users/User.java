package com.hongsolo.taskspree.modules.users.domain.users;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", schema = "users")
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String identityId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    // Private constructor - Forces use of the Factory Method
    private User(
            String identityId,
            String email,
            String username)
    {
        this.identityId = identityId;
        this.email = email;
        this.username = username;
    }

    public static User create(String identityId, String email) {
        String username = email.split("@")[0];

        return new User(identityId, email, username);
    }
}