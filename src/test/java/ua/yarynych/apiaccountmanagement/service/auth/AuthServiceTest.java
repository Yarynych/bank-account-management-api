package ua.yarynych.apiaccountmanagement.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.auth.Token;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.AuthResponse;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.LoginRequest;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.RegistrationRequest;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;
import ua.yarynych.apiaccountmanagement.entity.exceptions.BadTokenException;
import ua.yarynych.apiaccountmanagement.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService tokenProvider;
    @Mock
    private PasswordEncoder encoder;
    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserAuthDetails authUserDetails;
    private LoginRequest loginRequest;
    private RegistrationRequest registrationRequest;
    private AuthResponse authResponse;
    private String validToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setToken("existingToken");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        registrationRequest = new RegistrationRequest();
        registrationRequest.setFirstName("first");
        registrationRequest.setSecondName("second");
        registrationRequest.setPhone("phone");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password");
        registrationRequest.setRole(Role.ROLE_INTERNAL_USER);

        authResponse = new AuthResponse("test", "name");
        authResponse.setEmail("test@example.com");
        authResponse.setAccessToken(new Token("newAccessToken", 259200000));

        validToken = "Bearer newAccessToken";
        refreshToken = "Bearer refreshToken";

        authUserDetails = UserAuthDetails.build(testUser.getEmail(), Role.ROLE_INTERNAL_USER.name());
    }

    @Test
    void testLoginSuccess() {
        when(userService.findUserByEmailForAuthorization(loginRequest.getEmail())).thenReturn(testUser);
        when(encoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(tokenProvider.token(testUser)).thenReturn(authResponse);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("newAccessToken", response.getAccessToken().getToken());
        verify(userService).saveUser(testUser);
    }

    @Test
    void testLoginUserNotFound() {
        when(userService.findUserByEmailForAuthorization(loginRequest.getEmail())).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLoginWrongPassword() {
        when(userService.findUserByEmailForAuthorization(loginRequest.getEmail())).thenReturn(testUser);
        when(encoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testRegistrationUserAlreadyExists() {
        when(userService.findUserByEmailForAuthorization(registrationRequest.getEmail())).thenReturn(testUser);

        assertThrows(BadCredentialsException.class, () -> authService.registration(registrationRequest));
    }

    @Test
    void testLogoutSuccess() {
        when(tokenProvider.read(anyString())).thenReturn(authUserDetails);
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(testUser);

        AuthResponse response = authService.logout(validToken);

        assertNotNull(response);
        assertNull(testUser.getToken());
        verify(userService).saveUser(testUser);
    }

    @Test
    void testRefreshTokenInvalidFormat() {
        assertThrows(BadTokenException.class, () -> authService.refreshToken("InvalidToken"));
    }

    @Test
    void testRefreshTokenSuccess() {
        when(tokenProvider.read(anyString())).thenReturn(authUserDetails);
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(testUser);
        when(tokenProvider.token(testUser)).thenReturn(authResponse);

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userService).saveUser(testUser);
    }

    @Test
    void testRegistrationSuccess() {
        when(userService.findUserByEmailForAuthorization(registrationRequest.getEmail())).thenReturn(null);
        when(encoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(tokenProvider.token(any(User.class))).thenReturn(authResponse);

        AuthResponse response = authService.registration(registrationRequest);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userService).saveUser(any(User.class));
    }
}
