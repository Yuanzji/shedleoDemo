package com.shedleo.payment.client;

import com.shedleo.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 支付服务 Feign 接口
 *
 * 【壳子说明】：此处只定义 Feign 接口 + Controller 空实现。
 *  实际项目中需要对接微信支付、支付宝等第三方支付平台：
 *  - 调用统一下单接口，返回支付参数
 *  - 接收第三方异步回调通知，更新支付状态
 *  - 退款调用商户退款 API
 *  - 结算调用分账/资金结算 API
 */
@FeignClient(name = "shedleo-payment")
public interface PaymentClient {

    @PostMapping("/payment/refund")
    Result<Boolean> refund(@RequestParam("orderNo") String orderNo,
                           @RequestParam("amount") BigDecimal amount);

    @PostMapping("/payment/settle")
    Result<Boolean> settle(@RequestParam("merchantId") Long merchantId,
                           @RequestParam("amount") BigDecimal amount);
}
