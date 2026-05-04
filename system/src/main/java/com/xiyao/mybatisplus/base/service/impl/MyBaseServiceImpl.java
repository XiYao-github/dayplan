package com.xiyao.mybatisplus.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyao.mybatisplus.base.mapper.MyBaseMapper;

public abstract class MyBaseServiceImpl<M extends MyBaseMapper<T>, T> extends ServiceImpl<M, T> {

}
