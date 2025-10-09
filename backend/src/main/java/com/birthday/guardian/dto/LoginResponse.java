package com.birthday.guardian.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String membershipLevel;
    private LocalDateTime vipExpireTime;
    private Integer maxRoleCount;
    private Boolean vipActive;
}
