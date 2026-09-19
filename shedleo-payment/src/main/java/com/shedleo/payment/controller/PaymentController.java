package com.shedleo.payment.controller;

import com.shedleo.common.result.Result;
import com.shedleo.payment.client.PaymentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 支付服务壳 —— Controller
 *
 * 【实际职责】：
 *  1. 对接微信/支付宝统一下单 API，生成支付二维码或跳转链接
 *  2. 接收第三方支付异步回调（notify_url），验签后更新订单支付状态
 *  3. 退款 —— 调用第三方商户退款 API，记录退款流水
 *  4. 结算/分账 —— 确认收货后调用分账 API，将资金冻结部分结算给商家
 *  5. 对账 —— 定时拉取第三方账单与本地流水核对
 *
 *  当前仅为演示壳子，所有方法只打印日志返回成功。
 */
@Slf4j
@RestController
public class PaymentController implements PaymentClient {

    @Override
    public Result<Boolean> refund(String orderNo, BigDecimal amount) {
        log.info("[PAYMENT-SHELL] 收到退款请求: orderNo={}, amount={}", orderNo, amount);
        log.info("[PAYMENT-SHELL] 此处应调用微信/支付宝退款 API，当前为演示壳子");
        return Result.success(true);
    }

    @Override
    public Result<Boolean> settle(Long merchantId, BigDecimal amount) {
        log.info("[PAYMENT-SHELL] 收到结算请求: merchantId={}, amount={}", merchantId, amount);
        log.info("[PAYMENT-SHELL] 此处应调用微信/支付宝分账 API，当前为演示壳子");
        return Result.success(true);
    }
}
