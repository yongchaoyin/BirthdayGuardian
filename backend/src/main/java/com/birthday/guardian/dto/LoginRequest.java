package com.birthday.guardian.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名/邮箱不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
