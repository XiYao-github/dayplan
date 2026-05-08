package com.xiyao.common.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.common.base.mapper.MyBaseMapper;

public abstract class MyBaseServiceImpl<M extends MyBaseMapper<T>, T> extends ServiceImpl<M, T> {

}
