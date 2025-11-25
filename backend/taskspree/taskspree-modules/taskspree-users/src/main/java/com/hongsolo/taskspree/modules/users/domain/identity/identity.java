package com.hongsolo.taskspree.modules.users.domain.identity;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "identity", schema = "users")
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class identity extends BaseEntity {

    @Column
    private String email;

}
