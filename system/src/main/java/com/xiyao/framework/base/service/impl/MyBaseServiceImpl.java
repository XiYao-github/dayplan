package com.xiyao.framework.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.framework.base.mapper.MyBaseMapper;

public abstract class MyBaseServiceImpl<M extends MyBaseMapper<T>, T> extends ServiceImpl<M, T> {

}
