package com.gongcha.berrymatch.user;

import com.gongcha.berrymatch.springSecurity.constants.ProviderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdentifier(String identifier);

    @Query("select u from User u where u.identifier = :identifier")
    List<User> findAllByIdentifier(String identifier);

    Optional<User> findByNickname(String nickname);

    @Query("select u from User u where u.identifier = :identifier and u.providerInfo = :providerInfo")
    Optional<User> findByOAuthInfo(@Param("identifier") String identifier,
                                   @Param("providerInfo") ProviderInfo providerInfo);

}