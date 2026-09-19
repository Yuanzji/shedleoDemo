package com.shedleo.user.controller;

import com.shedleo.common.result.Result;
import com.shedleo.user.client.UserClient;
import com.shedleo.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务壳 —— Controller
 *
 * 【实际职责】：
 *  - 用户注册/登录（微信扫码、手机验证码、账号密码）
 *  - JWT/OAuth2 Token 颁发与验证
 *  - 用户信息 CRUD
 *  - 收货地址管理
 *  - 会员等级、积分
 *  - 实名认证接口对接
 *
 * 当前返回模拟用户数据。
 */
@Slf4j
@RestController
public class UserController implements UserClient {

    @Override
    public Result<UserDTO> getUserById(Long userId) {
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setUsername("user_" + userId);
        user.setNickname("Shedleo 用户" + userId);
        user.setPhone("138****" + String.format("%04d", userId % 10000));
        user.setAvatar("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cartoon%20user%20avatar&image_size=square");
        user.setStatus(1);
        return Result.success(user);
    }
}
