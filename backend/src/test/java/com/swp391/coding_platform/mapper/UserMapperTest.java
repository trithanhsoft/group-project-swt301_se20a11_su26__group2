package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.request.RegisterRequest;
import com.swp391.coding_platform.dto.response.AuthenticationResponse;
import com.swp391.coding_platform.dto.response.UserResponse;
import com.swp391.coding_platform.entity.auth.RoleEntity;
import com.swp391.coding_platform.entity.enums.RoleName;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    void toAuthenticationResponse() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("test");
        user.setDisplayname("Test User");
        user.setAvatarurl("http://avatar.com");
        user.setEmail("test@test.com");

        RoleEntity role = new RoleEntity();
        role.setName(RoleName.USER);
        user.setRoles(Set.of(role));

        AuthenticationResponse response = mapper.toAuthenticationResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test", response.getUsername());
        assertEquals("Test User", response.getDisplayName());
        assertEquals("http://avatar.com", response.getAvatarUrl());
        assertEquals("test@test.com", response.getEmail());
        assertTrue(response.getRoles().contains("USER"));
    }

    @Test
    void toUserEntity() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@new.com");
        request.setDisplayname("New User");

        UserEntity user = mapper.toUserEntity(request);

        assertNotNull(user);
        assertEquals("newuser", user.getUsername());
        assertEquals("new@new.com", user.getEmail());
        assertEquals("New User", user.getDisplayname());
        assertEquals(com.swp391.coding_platform.entity.enums.UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void toUserResponse() {
        UserEntity user = new UserEntity();
        user.setId(2);
        user.setUsername("user2");
        user.setDisplayname("User Two");
        user.setAvatarurl("http://avatar2.com");

        UserResponse response = mapper.toUserResponse(user);

        assertNotNull(response);
        assertEquals(2L, response.getId());

        assertEquals("User Two", response.getDisplayName());
        assertEquals("http://avatar2.com", response.getAvatarUrl());
    }

    @Test
    void mapRolesNull() {
        assertNull(mapper.map(null));
    }
}
