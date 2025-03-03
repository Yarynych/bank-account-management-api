package ua.yarynych.apiaccountmanagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.entity.*;
import ua.yarynych.apiaccountmanagement.entity.dto.user.UserUpdateRequest;
import ua.yarynych.apiaccountmanagement.entity.exceptions.DatabaseNotFoundException;
import ua.yarynych.apiaccountmanagement.repository.UserRepository;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService tokenProvider;

    public List<User> getAllUsers(String token) {
        log.info("Fetching all users from the repository");

        if(!validateUserAuthStatus(token)) {
            throw new BadCredentialsException("Differences between saved and taken tokens");
        }

        return userRepository.findAll();
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", email);
                    return new BadCredentialsException("Bad credentials: no user with this email");
                });
    }

    public User findUserByEmail(String email, String token) {
        log.info("Searching for user with email: {}", email);

        if(!validateUserAuthStatus(token)) {
            throw new BadCredentialsException("Differences between saved and taken tokens");
        }

        return findByEmail(email);
    }

    public User findUserById(Long id, String token) {
        log.info("Fetching user with id: {}", id);

        if(!validateUserAuthStatus(token)) {
            throw new BadCredentialsException("Differences between saved and taken tokens");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new DatabaseNotFoundException("User not found"));
    }

    public User findUserByEmailForAuthorization(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void saveUser(User user) {
        log.info("Saving new user: {}", user.getEmail());
        userRepository.save(user);
    }

    public String deleteUserById(Long userId, String token) {
        log.info("Deleting user with id: {}", userId);

        if(!validateUserAuthStatus(token)) {
            throw new BadCredentialsException("Differences between saved and taken tokens");
        }

        userRepository.deleteById(userId);
        return "User deleted successfully";
    }

    @Transactional
    public User updateUser(UserUpdateRequest updateRequest, String token) {
        token = token.replace("Bearer ", "");
        String userEmail = tokenProvider.read(token).getEmail();
        User userToUpdate = findUserByEmail(userEmail, token);

        if (updateRequest.getFirstName() != null) userToUpdate.setFirstName(updateRequest.getFirstName());
        if (updateRequest.getSecondName() != null) userToUpdate.setSecondName(updateRequest.getSecondName());
        if (updateRequest.getEmail() != null) userToUpdate.setEmail(updateRequest.getEmail());
        if (updateRequest.getPhone() != null) userToUpdate.setPhone(updateRequest.getPhone());

        log.info("Saving updated user: {}", userToUpdate.getEmail());
        userRepository.save(userToUpdate);

        return userToUpdate;
    }

    public Boolean validateUserAuthStatus(String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = findByEmail(email);

        return token.equals(user.getToken());
    }
}

