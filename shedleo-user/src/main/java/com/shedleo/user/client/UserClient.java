package com.shedleo.user.client;

import com.shedleo.common.result.Result;
import com.shedleo.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 接口
 *
 * 【实际职责】：
 *  - 用户注册/登录（支持微信扫码、手机号验证码、账号密码）
 *  - JWT / OAuth2 Token 颁发与验证
 *  - 用户信息 CRUD
 *  - 收货地址管理
 *  - 会员等级、积分管理
 *  - 实名认证
 */
@FeignClient(name = "shedleo-user")
public interface UserClient {

    @GetMapping("/user/info")
    Result<UserDTO> getUserById(@RequestParam("userId") Long userId);
}
