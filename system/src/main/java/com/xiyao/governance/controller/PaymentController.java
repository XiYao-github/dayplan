package com.xiyao.governance.controller;


import com.xiyao.governance.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/pay")
    public CompletableFuture<String> pay(@RequestParam String orderId, @RequestParam Double amount) {
        log.info("收到支付请求，订单号: {}", orderId);
        return paymentService.pay(orderId, amount);
    }
}