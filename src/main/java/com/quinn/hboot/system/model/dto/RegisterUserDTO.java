package com.quinn.hboot.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yu-qh
 */
@Data
public class RegisterUserDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "机构ID不能为空")
    private Long orgId;

    private String nickname;
}

