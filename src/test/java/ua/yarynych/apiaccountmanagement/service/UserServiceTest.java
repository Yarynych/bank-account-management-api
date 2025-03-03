package ua.yarynych.apiaccountmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.entity.dto.user.UserUpdateRequest;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;
import ua.yarynych.apiaccountmanagement.entity.exceptions.DatabaseNotFoundException;
import ua.yarynych.apiaccountmanagement.repository.UserRepository;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService tokenProvider;

    @Spy
    @InjectMocks
    private UserService userService;

    private User user;
    private String validToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setToken("validToken");

        validToken = "Bearer validToken";

        lenient().when(tokenProvider.read("validToken")).thenReturn(UserAuthDetails.build(user.getEmail(), Role.ROLE_INTERNAL_USER.name()));
    }

    @Test
    void testGetAllUsersSuccess() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.validateUserAuthStatus("Bearer validToken")).thenReturn(true);

        List<User> users = userService.getAllUsers("Bearer validToken");

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("test@example.com", users.get(0).getEmail());
    }



    @Test
    void testGetAllUsersInvalidToken() {
        String invalidToken = "invalidToken";

        doReturn(false).when(userService).validateUserAuthStatus(invalidToken);

        assertThrows(BadCredentialsException.class, () -> userService.getAllUsers(invalidToken));

        verify(userRepository, never()).findAll();
    }

    @Test
    void testFindUserByEmailSuccess() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doReturn(true).when(userService).validateUserAuthStatus("validToken"); // Використовуємо doReturn

        User foundUser = userService.findUserByEmail("test@example.com", "validToken");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testFindUserByEmailNotFound() {
        lenient().when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.findUserByEmail("notfound@example.com", validToken));
    }

    @Test
    void testFindUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.validateUserAuthStatus("Bearer validToken")).thenReturn(true);

        User foundUser = userService.findUserById(1L, "Bearer validToken");

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
    }



    @Test
    void testFindUserByIdNotFound() {
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.findUserById(2L, validToken));
    }

    @Test
    void testFindUserByEmailForAuthorizationSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByEmailForAuthorization("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testFindUserByEmailForAuthorizationNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        User foundUser = userService.findUserByEmailForAuthorization("notfound@example.com");

        assertNull(foundUser);
    }

    @Test
    void testSaveUser() {
        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUserByIdSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.validateUserAuthStatus("Bearer validToken")).thenReturn(true);

        String result = userService.deleteUserById(1L, "Bearer validToken");

        assertEquals("User deleted successfully", result);
        verify(userRepository, times(1)).deleteById(1L);
    }



    @Test
    void testDeleteUserByIdInvalidToken() {
        String invalidToken = "invalidToken";

        doReturn(false).when(userService).validateUserAuthStatus(anyString());

        assertThrows(BadCredentialsException.class, () -> userService.deleteUserById(1L, invalidToken));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateUserSuccess() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("NewName");
        updateRequest.setEmail("new@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.validateUserAuthStatus("validToken")).thenReturn(true);

        User updatedUser = userService.updateUser(updateRequest, validToken);

        assertEquals("NewName", updatedUser.getFirstName());
        assertEquals("new@example.com", updatedUser.getEmail());
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void testValidateUserAuthStatusValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        boolean isValid = userService.validateUserAuthStatus(validToken);

        assertTrue(isValid);
    }
}