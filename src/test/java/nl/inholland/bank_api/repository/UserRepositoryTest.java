package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User approvedCustomer;
    private User pendingEmployee;

    @BeforeEach
    void setUp() {
        approvedCustomer = User.builder()
                .firstName("Anna")
                .lastName("Smith")
                .email("anna@mail.com")
                .password("pw")
                .bsn("123456789")
                .phoneNumber("+31600000001")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        pendingEmployee = User.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob@mail.com")
                .password("pw")
                .bsn("987654321")
                .phoneNumber("+31600000002")
                .isApproved(UserAccountStatus.PENDING)
                .role(UserRole.EMPLOYEE)
                .build();

        userRepository.saveAll(List.of(approvedCustomer, pendingEmployee));
    }

    @Test
    void existsByEmailShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("anna@mail.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByBsnShouldReturnTrue() {
        boolean exists = userRepository.existsByBsn("987654321");
        assertThat(exists).isTrue();
    }

    @Test
    void findByIsApprovedShouldReturnCorrectUsers() {
        List<User> approved = userRepository.findByIsApproved(UserAccountStatus.APPROVED);

        assertThat(approved).hasSize(1);
        assertThat(approved.get(0).getEmail()).isEqualTo("anna@mail.com");
    }

    @Test
    void findByIsApprovedAndRoleShouldReturnCorrectUser() {
        List<User> pendingEmployees = userRepository.findByIsApprovedAndRole(UserAccountStatus.PENDING, UserRole.EMPLOYEE);

        assertThat(pendingEmployees).hasSize(1);
        assertThat(pendingEmployees.get(0).getEmail()).isEqualTo("bob@mail.com");
    }

    @Test
    void findByEmailShouldReturnUser() {
        Optional<User> user = userRepository.findByEmail("anna@mail.com");

        assertThat(user).isPresent();
        assertThat(user.get().getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void findByIdShouldReturnUserDirectly() {
        Optional<User> optionalUser = userRepository.findById(approvedCustomer.getId());

        assertThat(optionalUser).isPresent();

        User found = optionalUser.get();
        assertThat(found.getEmail()).isEqualTo("anna@mail.com");
    }
}