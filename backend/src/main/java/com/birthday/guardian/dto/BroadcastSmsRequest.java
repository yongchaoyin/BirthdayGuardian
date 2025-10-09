package com.birthday.guardian.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BroadcastSmsRequest {
    @NotBlank(message = "短信内容不能为空")
    private String content;
}
