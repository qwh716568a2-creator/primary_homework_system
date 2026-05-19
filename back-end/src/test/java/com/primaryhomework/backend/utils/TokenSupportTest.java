package com.primaryhomework.backend.utils;

import com.primaryhomework.backend.entity.po.UserPo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenSupportTest {

    @Test
    void shouldParseValidToken() {
        UserPo user = new UserPo();
        user.setId(1001L);
        user.setRoleType("teacher");

        String token = TokenSupport.buildToken(user);
        TokenSupport.ParsedToken parsedToken = TokenSupport.parseAuthorization("Bearer " + token);

        assertNotNull(parsedToken);
        assertEquals(1001L, parsedToken.userId());
        assertEquals("teacher", parsedToken.roleType());
        assertTrue(parsedToken.expireAt() > 0);
    }

    @Test
    void shouldRejectTamperedToken() {
        UserPo user = new UserPo();
        user.setId(1002L);
        user.setRoleType("student");

        String token = TokenSupport.buildToken(user);
        String tamperedToken = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertNull(TokenSupport.parseToken(tamperedToken));
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = TokenSupport.buildToken(1003L, "admin", 1L);

        assertNull(TokenSupport.parseToken(expiredToken));
    }
}
