package com.example.starter_kit_restapi_springboot.repository;

import com.example.starter_kit_restapi_springboot.entity.Token;
import com.example.starter_kit_restapi_springboot.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("select t from Token t inner join User u on t.user.id = u.id where u.id = :id and (t.expired = false or t.revoked = false)")
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);

    Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.user.id = :userId AND t.tokenType = 'BEARER'")
    void deleteAllBearerTokensByUserId(Long userId);
}