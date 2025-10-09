package com.birthday.guardian.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BroadcastEmailRequest {
    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    @NotBlank(message = "邮件内容不能为空")
    private String content;
}
