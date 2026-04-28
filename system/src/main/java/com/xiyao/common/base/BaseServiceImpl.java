package com.xiyao.common.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public abstract class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

}
