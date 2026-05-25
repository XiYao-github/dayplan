package com.xiyao.trace.controller;

import com.xiyao.common.utils.Result;
import com.xiyao.trace.annotation.Trace;
import com.xiyao.trace.context.TraceContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 链路追踪测试控制器
 *
 * @author xiyao
 */
@Slf4j
@RestController
@RequestMapping("/trace")
public class TraceTestController {

    /**
     * 测试简单追踪
     */
    @Trace(module = "链路追踪", operation = "测试接口")
    @GetMapping("/test")
    public Result test() {
        log.info("这是一条测试日志，查看是否携带 traceId");
        return Result.success("链路追踪正常");
    }

    /**
     * 测试带参数的追踪
     */
    @Trace(module = "链路追踪", operation = "带参数测试")
    @GetMapping("/test/params")
    public Result testWithParams(@RequestParam String name, @RequestParam Integer age) {
        log.info("收到请求参数: name={}, age={}", name, age);
        return Result.success(new User(name, age));
    }

    /**
     * 测试异常追踪
     */
    @Trace(module = "链路追踪", operation = "异常测试")
    @GetMapping("/test/error")
    public Result testError() {
        throw new RuntimeException("这是故意的测试异常");
    }

    /**
     * 获取当前 traceId
     */
    @GetMapping("/current")
    public Result getCurrentTrace() {
        TraceContext context = TraceContext.get();
        TraceInfo info = new TraceInfo();
        if (context != null) {
            info.setTraceId(context.getTraceId());
            info.setSpanId(context.getSpanId());
        }
        return Result.success(info);
    }

    @Data
    public static class User {
        private String name;
        private Integer age;

        public User() {}
        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

    @Data
    public static class TraceInfo {
        private String traceId;
        private String spanId;
    }
}