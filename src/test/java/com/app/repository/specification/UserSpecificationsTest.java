package com.app.repository.specification;

import com.app.entity.UserEntity;
import com.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.app.factory.UserTestFactory.createDefaultUser;
import static com.app.repository.specification.UserSpecifications.emailContains;
import static com.app.repository.specification.UserSpecifications.usernameContains;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserSpecificationsTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnUsersWithMatchingEmailWhenEmailProvided() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("userOne@test.com");
        userRepository.save(user);

        Specification<UserEntity> spec = emailContains("one");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getEmail)
                .containsExactly("userOne@test.com");
    }

    @Test
    void shouldReturnUsersWithMatchingEmailCaseInsensitive() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("userOne@test.com");
        userRepository.save(user);

        Specification<UserEntity> spec = emailContains("ONE");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getEmail)
                .containsExactly("userOne@test.com");
    }

    @Test
    void shouldReturnUsersWithPartialEmailMatch() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("userTwo@test.com");
        userRepository.save(user);

        Specification<UserEntity> spec = emailContains("test");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getEmail)
                .containsExactly("userTwo@test.com");
    }

    @Test
    void shouldReturnEmptyListWhenNoEmailMatches() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("test@test.com");
        userRepository.save(user);

        Specification<UserEntity> spec = emailContains("nonexistent");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUsersWithMatchingUsernameWhenUsernameProvided() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setUsername("userTwo");
        userRepository.save(user);

        Specification<UserEntity> spec = usernameContains("two");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getUsername)
                .containsExactly("userTwo");
    }

    @Test
    void shouldReturnUsersWithMatchingUsernameCaseInsensitive() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setUsername("admin");
        userRepository.save(user);

        Specification<UserEntity> spec = usernameContains("ADMIN");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getUsername)
                .containsExactly("admin");
    }

    @Test
    void shouldReturnUsersWithPartialUsernameMatch() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setUsername("userOne");
        userRepository.save(user);

        Specification<UserEntity> spec = usernameContains("one");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getUsername)
                .containsExactly("userOne");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsernameMatches() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setUsername("testuser");
        userRepository.save(user);

        Specification<UserEntity> spec = usernameContains("nonexistent");
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUsersMatchingBothEmailAndUsername() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("userOne@test.com");
        user.setUsername("userOne");
        userRepository.save(user);

        Specification<UserEntity> spec = Specification.where(emailContains("one"))
                .and(usernameContains("one"));
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getEmail)
                .containsExactly("userOne@test.com");
    }

    @Test
    void shouldReturnEmptyListWhenNoUserMatchesBothCriteria() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("userOne@test.com");
        user.setUsername("userOne");
        userRepository.save(user);

        Specification<UserEntity> spec = Specification.where(emailContains("john"))
                .and(usernameContains("smith"));
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnFilteredUsersWhenOneSpecificationIsNull() {
        UserEntity user = createDefaultUser();
        user.setId(null);
        user.setEmail("userTwo@test.com");
        user.setUsername("userTwo");
        userRepository.save(user);

        Specification<UserEntity> spec = Specification.where(emailContains("test"))
                .and(usernameContains(null));
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(UserEntity::getEmail)
                .containsExactly("userTwo@test.com");
    }

    @Test
    void shouldReturnAllUsersWhenBothSpecificationsAreNull() {
        UserEntity user1 = createDefaultUser();
        user1.setId(null);
        user1.setEmail("user1@test.com");
        UserEntity user2 = createDefaultUser();
        user2.setId(null);
        user2.setEmail("user2@test.com");
        UserEntity user3 = createDefaultUser();
        user3.setId(null);
        user3.setEmail("user3@test.com");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Specification<UserEntity> spec = Specification.where(emailContains(null))
                .and(usernameContains(null));
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result).hasSize(3);
    }

    @Test
    void shouldWorkWithPagination() {
        UserEntity user1 = createDefaultUser();
        user1.setId(null);
        user1.setEmail("user1@test.com");
        UserEntity user2 = createDefaultUser();
        user2.setId(null);
        user2.setEmail("user2@test.com");
        UserEntity user3 = createDefaultUser();
        user3.setId(null);
        user3.setEmail("user3@test.com");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Specification<UserEntity> spec = emailContains("@");
        Pageable pageable = PageRequest.of(0, 2);
        Page<UserEntity> result = userRepository.findAll(spec, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldWorkWithOrCombination() {
        UserEntity user1 = createDefaultUser();
        user1.setId(null);
        user1.setEmail("userOne@test.com");
        user1.setUsername("userOne");
        UserEntity user2 = createDefaultUser();
        user2.setId(null);
        user2.setEmail("admin@test.com");
        user2.setUsername("admin");
        userRepository.save(user1);
        userRepository.save(user2);

        Specification<UserEntity> spec = Specification.where(emailContains("one"))
                .or(usernameContains("admin"));
        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(2)
                .extracting(UserEntity::getUsername)
                .containsExactlyInAnyOrder("userOne", "admin");
    }

    @Test
    void shouldHandleComplexCombinations() {
        UserEntity user1 = createDefaultUser();
        user1.setId(null);
        user1.setEmail("userOne@test.com");
        user1.setUsername("userOne");
        UserEntity user2 = createDefaultUser();
        user2.setId(null);
        user2.setEmail("userTwo@test.com");
        user2.setUsername("userTwo");
        UserEntity user3 = createDefaultUser();
        user3.setId(null);
        user3.setEmail("admin@test.com");
        user3.setUsername("admin");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Specification<UserEntity> spec = Specification.where(
                        Specification.where(emailContains("example"))
                                .or(emailContains("test"))
                )
                .and(usernameContains("two")
                        .or(usernameContains("one")));

        List<UserEntity> result = userRepository.findAll(spec);

        assertThat(result)
                .hasSize(2)
                .extracting(UserEntity::getUsername)
                .containsExactlyInAnyOrder( "userTwo", "userOne");
    }
}