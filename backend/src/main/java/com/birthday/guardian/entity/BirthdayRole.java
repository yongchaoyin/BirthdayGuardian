package com.birthday.guardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("birthday_role")
public class BirthdayRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String roleType;

    private String roleName;

    private LocalDate birthDate;

    private Integer calendarType;

    private String lunarBirthDate;

    private Integer remindDays;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
