package com.shedleo.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息 DTO —— 模拟
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer status;
}
