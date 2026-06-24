package com.quinn.hboot.system.model.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {

    private Long userId;

    private String newPassword;
}
