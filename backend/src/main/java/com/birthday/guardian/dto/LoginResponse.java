package com.birthday.guardian.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String role;
}
