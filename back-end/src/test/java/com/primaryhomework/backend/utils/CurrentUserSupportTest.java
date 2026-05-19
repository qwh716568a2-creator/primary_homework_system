package com.primaryhomework.backend.utils;

import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserSupportTest {

    @Mock
    private UserMapper userMapper;

    @Test
    void shouldReturnUserWhenAuthorizationIsValid() {
        UserPo user = new UserPo();
        user.setId(2001L);
        user.setRoleType("admin");
        user.setStatus("enabled");
        when(userMapper.selectById(2001L)).thenReturn(user);

        String token = TokenSupport.buildToken(user);
        UserPo currentUser = CurrentUserSupport.requireUser("Bearer " + token, "admin", userMapper);

        assertSame(user, currentUser);
    }

    @Test
    void shouldRejectMissingAuthorization() {
        CommonException exception = assertThrows(
                CommonException.class,
                () -> CurrentUserSupport.requireUser(null, "teacher", userMapper)
        );

        assertEquals(40101, exception.getCode());
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldRejectRoleMismatch() {
        UserPo user = new UserPo();
        user.setId(2002L);
        user.setRoleType("student");
        String token = TokenSupport.buildToken(user);

        CommonException exception = assertThrows(
                CommonException.class,
                () -> CurrentUserSupport.requireUser(token, "teacher", userMapper)
        );

        assertEquals(40101, exception.getCode());
        verifyNoInteractions(userMapper);
    }
}
