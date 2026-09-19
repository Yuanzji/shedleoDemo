package com.shedleo.message.controller;

import com.shedleo.common.result.Result;
import com.shedleo.message.client.MessageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息服务壳 —— Controller
 *
 * 【实际职责】：
 *  - 短信发送（对接阿里云/腾讯云短信）
 *  - 站内信/系统通知（需持久化到 DB）
 *  - 邮件发送（SMTP / 云邮件推送）
 *  - App 推送（极光、个推、友盟）
 *  - 短信频率限制（防刷、验证码 60s 间隔）
 *  - 消息模板管理（审核）
 *
 * 当前仅打印日志模拟发送。
 */
@Slf4j
@RestController
public class MessageController implements MessageClient {

    @Override
    public Result<Boolean> sendSms(String phone, String content) {
        log.info("[MESSAGE-SHELL] ★ 模拟发送短信 → {} : {} ★", phone, content);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> sendStationLetter(Long userId, String title, String content) {
        log.info("[MESSAGE-SHELL] ★ 模拟发送站内信 → 用户[{}]: [{}] {} ★", userId, title, content);
        return Result.success(true);
    }
}
