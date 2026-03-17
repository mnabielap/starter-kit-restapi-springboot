package com.example.starter_kit_restapi_springboot.model;

import com.example.starter_kit_restapi_springboot.dto.response.AuthResponse;
import com.example.starter_kit_restapi_springboot.dto.response.PagedResponse;
import com.example.starter_kit_restapi_springboot.dto.response.UserResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.Token;
import com.example.starter_kit_restapi_springboot.entity.TokenType;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.exception.DuplicateResourceException;
import com.example.starter_kit_restapi_springboot.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainSupportTest {

    @Test
    void roleFromStringShouldHandleNullAndCaseInsensitiveValues() {
        assertThat(Role.fromString(null)).isNull();
        assertThat(Role.fromString("admin")).isEqualTo(Role.ADMIN);
    }

    @Test
    void userShouldExposeSecurityContractAndLifecycleMethods() {
        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.ADMIN)
                .build();

        ReflectionTestUtils.invokeMethod(user, "onCreate");
        LocalDateTime createdAt = user.getCreatedAt();
        ReflectionTestUtils.invokeMethod(user, "onUpdate");

        assertThat(user.getAuthorities()).extracting("authority").containsExactly("ADMIN");
        assertThat(user.getUsername()).isEqualTo("alice@example.com");
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void userResponseFromUserShouldMapFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(2L)
                .name("Alice")
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .isEmailVerified(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserResponse response = UserResponse.fromUser(user);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.isEmailVerified()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void pagedResponseFromPageShouldUseOneBasedPageIndex() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        PagedResponse<Integer> response = PagedResponse.fromPage(page, List.of(1, 2));

        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getLimit()).isEqualTo(2);
        assertThat(response.getTotalResults()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    void tokenAndAuthResponseBuildersShouldPopulateFields() {
        Date expiry = new Date();
        Token token = Token.builder()
                .token("access-token")
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        AuthResponse response = AuthResponse.builder()
                .user(UserResponse.builder().id(3L).build())
                .tokens(AuthResponse.TokensWrapper.builder()
                        .access(AuthResponse.TokenDetail.builder().token("access-token").expires(expiry).build())
                        .refresh(AuthResponse.TokenDetail.builder().token("refresh-token").expires(expiry).build())
                        .build())
                .build();

        assertThat(token.getTokenType()).isEqualTo(TokenType.BEARER);
        assertThat(response.getTokens().getAccess().getExpires()).isEqualTo(expiry);
        assertThat(response.getTokens().getRefresh().getToken()).isEqualTo("refresh-token");
    }

    @Test
    void customExceptionsShouldKeepTheirMessages() {
        assertThat(new DuplicateResourceException("duplicate")).hasMessage("duplicate");
        assertThat(new ResourceNotFoundException("missing")).hasMessage("missing");
    }
}
