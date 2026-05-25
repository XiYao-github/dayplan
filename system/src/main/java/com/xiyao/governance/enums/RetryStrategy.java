package com.xiyao.governance.enums;

/**
 * 重试策略枚举
 * <p>
 * 定义两种重试策略：固定间隔和指数退避。
 *
 * <p>
 * <b>策略说明：</b>
 * <ul>
 *     <li>{@link #FIXED}：固定间隔重试，每次重试等待相同时间</li>
 *     <li>{@link #EXPONENTIAL}：指数退避重试，等待时间按倍数增长</li>
 * </ul>
 *
 * <p>
 * <b>使用场景：</b>
 * <ul>
 *     <li>FIXED：适用于对延迟敏感、失败原因简单明确的场景</li>
 *     <li>EXPONENTIAL：适用于网络不稳定、需要给下游服务恢复时间的场景</li>
 * </ul>
 *
 * <p>
 * <b>指数退避示例（interval=1000, multiplier=2.0）：</b>
 * <pre>
 * 第 1 次重试等待：1000ms
 * 第 2 次重试等待：2000ms
 * 第 3 次重试等待：4000ms
 * 第 4 次重试等待：8000ms
 * ...
 * </pre>
 *
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>指数退避可能导致等待时间过长，建议设置最大间隔</li>
 *     <li>可以配合 jitter（抖动）避免多实例同时重试造成惊群效应</li>
 *     <li>重试次数建议设置上限，避免无限重试</li>
 * </ul>
 *
 * @author xiyao
 * @see com.xiyao.governance.annotation.Retryable
 * @see com.xiyao.governance.core.retry.RetryAspect
 */
public enum RetryStrategy {

    /**
     * 固定间隔重试
     * <p>
     * 每次重试之间等待相同的时间间隔。
     * 适用于：
     * <ul>
     *     <li>对响应延迟敏感的业务</li>
     *     <li>失败原因是确定性的（如参数错误）</li>
     *     <li>需要快速反馈的场景</li>
     * </ul>
     */
    FIXED,

    /**
     * 指数退避重试
     * <p>
     * 每次重试的等待时间按指数增长。
     * 适用于：
     * <ul>
     *     <li>网络不稳定的场景</li>
     *     <li>下游服务可能暂时不可用</li>
     *     <li>需要避免大量重试同时涌入的场景</li>
     * </ul>
     */
    EXPONENTIAL
}