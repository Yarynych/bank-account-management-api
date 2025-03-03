package ua.yarynych.apiaccountmanagement.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.entity.*;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.AuthResponse;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.LoginRequest;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.LogoutRequest;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.RegistrationRequest;
import ua.yarynych.apiaccountmanagement.entity.exceptions.BadTokenException;
import ua.yarynych.apiaccountmanagement.service.UserService;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService tokenProvider;
    private final PasswordEncoder encoder;


    public AuthResponse login(LoginRequest request) {
        User user = userService.findUserByEmailForAuthorization(request.getEmail());

        if(user == null) {
            throw new BadCredentialsException("Bad credentials: no user with this email");
        }

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Bad credentials: incorrect password");
        }

        AuthResponse response = tokenProvider.token(user);
        user.setToken(response.getAccessToken().getToken());
        userService.saveUser(user);

        log.info("Login user with email: {}", response.getEmail());
        return response;
    }


    public AuthResponse registration(RegistrationRequest request) {
        if (null != userService.findUserByEmailForAuthorization(request.getEmail())) {
            throw new BadCredentialsException("User already exists");
        }

        if (null != request.getPassword()) {
            request.setPassword(encoder.encode(request.getPassword()));
        }

        User user = User.build(request);
        AuthResponse response = tokenProvider.token(user);
        user.setToken(response.getAccessToken().getToken());
        userService.saveUser(user);

        log.info("Registration user with email: {}", response.getEmail());
        return response;
    }


    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new BadTokenException("Incorrect refresh token header");
        }

        String token = refreshToken.substring(7);
        tokenProvider.validateToken(token);

        User owner = userService.findUserByEmail(tokenProvider.read(token).getEmail(), token);

        if(owner.getToken() == null) {
            throw new BadCredentialsException("You need authorize first");
        }

        AuthResponse response = tokenProvider.token(owner);
        owner.setToken(response.getAccessToken().getToken());
        userService.saveUser(owner);

        log.info("Refresh token user with email: {}", response.getEmail());
        return response;
    }


    public AuthResponse logout(String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        log.info("Logout by user {}", user);
        user.setToken(null);
        userService.saveUser(user);

        return AuthResponse.logout(user);
    }
}

