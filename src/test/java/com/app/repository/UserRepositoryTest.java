package com.app.repository;

import com.app.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static com.app.factory.UserTestFactory.createDefaultUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static com.app.factory.UserTestFactory.*;

@Slf4j
@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnTrueWhenEmailExists() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail(user.getEmail());
        assertTrue(exists);
    }

    @Test
    void shouldReturnUserWhenFindByUsername(){
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();
            UserEntity foundUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        // Assert
        assertNotNull(foundUser);
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getAge(), foundUser.getAge());
    }
    @Test
    void shouldReturnUserWhenFindByEmail() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();
        UserEntity foundUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        assertThat(foundUser)
                .isNotNull()
                .extracting(UserEntity::getUsername, UserEntity::getAge)
                .containsExactly(user.getUsername(), user.getAge());
    }

    @Test
    void shouldReturnUserWhenFilterByEmail() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();

        Page<UserEntity> results = userRepository.findByFilters("test", null, PageRequest.of(0, 10));
        // Assert
        assertThat(results)
                .isNotNull()
                .hasSize(1)
                .allSatisfy(foundUser -> {
                    assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
                    assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
                });
    }
    @Test
    void shouldReturnUserWhenFilterByUsername(){
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();
        Page<UserEntity> results =userRepository.findByFilters(null,"test", PageRequest.of(0, 10));
        // Assert
        assertThat(results)
                .isNotNull()
                .hasSize(1)
                .allSatisfy(foundUser ->{
                   assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
                   assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
                   assertThat(foundUser.getAge()).isEqualTo(user.getAge());
                });
    }
    @Test
    void shouldReturnEmptyWhenNoMatch(){
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();
        Page<UserEntity> results =userRepository.findByFilters(null,"nonexistent", PageRequest.of(0, 10));

        // Assert
        assertThat(results)
                .isNotNull()
                .hasSize(0);
    }
    @Test
    void shouldSaveUserSuccessfully() {
        UserEntity user = createDefaultUser();
        UserEntity saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertEquals(saved.getEmail(), user.getEmail());
        assertEquals(saved.getAge(), user.getAge());
    }
    @Test
    void shouldDeleteUserSuccessfully(){
        UserEntity user = createDefaultUser();
        user.setId(null);
        entityManager.persist(user);
        entityManager.flush();
        userRepository.delete(user);
        boolean exists = userRepository.existsByEmail("delete@gmail.com");
        assertThat(exists).isFalse();
    }

}
