package ua.yarynych.apiaccountmanagement.entity.dto.auth;

import lombok.*;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistrationRequest {
    private String firstName;
    private String secondName;
    private String phone;
    private Role role;
    private String email;
    private String password;
}
