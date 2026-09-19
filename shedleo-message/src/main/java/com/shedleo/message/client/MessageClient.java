package com.shedleo.message.client;

import com.shedleo.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 消息服务 Feign 接口
 *
 * 【实际职责】：
 *  - 短信发送：对接阿里云短信 / 腾讯云短信 / 华为云短信
 *  - 站内信/通知：用户中心消息通知、系统公告推送
 *  - 邮件发送：对接 SMTP / 阿里云邮件推送
 *  - 推送：极光推送 / 个推 / 友盟（App 端推送）
 *  - 短信/推送模板管理（验证码、营销类消息需审核）
 *  - 发送频率限制（验证码 60s 间隔、防刷）
 *  - 消息记录持久化，提供消息查询、已读标记
 */
@FeignClient(name = "shedleo-message")
public interface MessageClient {

    @PostMapping("/message/sms")
    Result<Boolean> sendSms(@RequestParam("phone") String phone,
                            @RequestParam("content") String content);

    @PostMapping("/message/station")
    Result<Boolean> sendStationLetter(@RequestParam("userId") Long userId,
                                      @RequestParam("title") String title,
                                      @RequestParam("content") String content);
}
