package com.birthday.guardian.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MiniProgramLoginRequest {
    @NotBlank(message = "code不能为空")
    private String code;
}
