package com.birthday.guardian.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePhoneRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
}
