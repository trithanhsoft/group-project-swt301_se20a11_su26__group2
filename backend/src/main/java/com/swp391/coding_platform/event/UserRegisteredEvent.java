package com.swp391.coding_platform.event;


import com.swp391.coding_platform.entity.user.UserEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegisteredEvent {
    UserEntity userEntity;
}
