package com.danielnavia.melodygenerator.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @Size(min = 3, max = 30)
    private String username;

    @Email
    private String email;

    @Size(min = 8, max = 100)
    private String password;
}
