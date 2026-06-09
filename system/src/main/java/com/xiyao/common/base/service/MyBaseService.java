package com.xiyao.common.base.service;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Service 基类接口
 * <p>
 * 继承 MyBatis-Plus 的 IService，提供通用的 CRUD 操作和分页方法。
 * 所有业务 Service 接口应继承此接口。
 *
 * <p>
 * <b>提供的通用方法：</b>
 * <ul>
 *     <li>save(T entity)：插入数据</li>
 *     <li>saveBatch(list)：批量插入</li>
 *     <li>updateById(T entity)：根据 ID 更新</li>
 *     <li>removeById(id)：根据 ID 删除</li>
 *     <li>getById(id)：根据 ID 查询</li>
 *     <li>list(wrapper)：条件查询列表</li>
 *     <li>page(page, wrapper)：分页查询</li>
 * </ul>
 *
 * @param <T> 实体类型
 * @author xiyao
 * @see IService
 */
public interface MyBaseService<T> extends IService<T> {

    /**
     * 筛选条件
     *
     * @param params map
     * @return QueryWrapper
     */
    // QueryWrapper<T> queryWrapper(Map<String, Object> params);

    /**
     * 关联处理
     *
     * @param list   源数据
     * @param params 筛选条件
     */
    // void handleList(List<T> list, Map<String, Object> params);

    // default IPage<T> getPage(Map<String, Object> params, String defaultOrderField, Boolean isAsc) {
    //     // 分页参数
    //     long curPage = 1;
    //     long limit = 10;
    //     if (!StrUtil.isBlankIfStr(params.get(Constant.PAGE))) {
    //         curPage = Long.parseLong(params.get(Constant.PAGE).toString());
    //     }
    //     if (!StrUtil.isBlankIfStr(params.get(Constant.LIMIT))) {
    //         limit = Long.parseLong(params.get(Constant.LIMIT).toString());
    //     }
    //     // 分页对象
    //     Page<T> page = new Page<>(curPage, limit);
    //     // 分页参数
    //     params.put(Constant.PAGE, page);
    //     // 前端字段排序
    //     if (!StrUtil.isBlankIfStr(params.get(Constant.ORDER_FIELD)) && !StrUtil.isBlankIfStr(params.get(Constant.ORDER))) {
    //         // 排序字段 防止SQL注入
    //         String orderField = SqlFilter.sqlInject(params.get(Constant.ORDER_FIELD).toString());
    //         String order = params.get(Constant.ORDER).toString();
    //         return page.addOrder(Constant.ASC.equalsIgnoreCase(order) ? OrderItem.asc(orderField) : OrderItem.desc(orderField));
    //     }
    //     // 没有排序字段，则不排序
    //     if (StrUtils.isBlank(defaultOrderField)) {
    //         return page;
    //     }
    //     // 默认排序
    //     return page.addOrder(isAsc ? OrderItem.asc(defaultOrderField) : OrderItem.desc(defaultOrderField));
    // }

    // default <E> PageData<E> queryPage(Map<String, Object> params, Class<E> target) {
    //     return new PageData<>(this.queryPage(params), target);
    // }
    //
    // default IPage<T> queryPage(Map<String, Object> params) {
    //     return this.queryPage(params, true);
    // }
    //
    // default IPage<T> queryPage(Map<String, Object> params, Boolean isHandle) {
    //     IPage<T> page = this.page(getPage(params, "", false), this.queryWrapper(params));
    //     if (isHandle && !page.getRecords().isEmpty()) {
    //         this.handleList(page.getRecords(), params);
    //     }
    //     return page;
    // }
    //
    // default List<T> queryList(Collection<? extends Serializable> idList) {
    //     List<T> list = this.listByIds(idList);
    //     if (!list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list;
    // }
    //
    // default List<T> queryList(Map<String, Object> params, Boolean isHandle) {
    //     List<T> list = this.list(this.queryWrapper(params));
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, params);
    //     }
    //     return list;
    // }
    //
    // default <E> List<E> queryList(Map<String, Object> params, Boolean isHandle, Class<E> target) {
    //     List<T> list = this.list(this.queryWrapper(params));
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, params);
    //     }
    //     return ConvertUtils.sourceToTarget(list, target);
    // }
    //
    // default List<T> queryList(Wrapper<T> queryWrapper, Boolean isHandle) {
    //     List<T> list = this.list(queryWrapper);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list;
    // }
    //
    // default <V> Map<V, T> queryMap(Collection<? extends Serializable> idList, Function<? super T, V> mapper, Boolean isHandle) {
    //     List<T> list = this.listByIds(idList);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.toMap(mapper, Function.identity()));
    // }
    //
    // default <V, U> Map<V, U> queryMap(Collection<? extends Serializable> idList, Function<? super T, V> keyMapper, Function<? super T, U> valueMapper, Boolean isHandle) {
    //     List<T> list = this.listByIds(idList);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    // }
    //
    // default <V> Map<V, T> queryMap(Wrapper<T> queryWrapper, Function<? super T, V> mapper, Boolean isHandle) {
    //     List<T> list = this.list(queryWrapper);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.toMap(mapper, Function.identity()));
    // }
    //
    // default <V, U> Map<V, U> queryMap(Wrapper<T> queryWrapper, Function<? super T, V> keyMapper, Function<? super T, U> valueMapper, Boolean isHandle) {
    //     List<T> list = this.list(queryWrapper);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    // }
    //
    // default <V> Map<V, List<T>> queryListMap(Collection<? extends Serializable> idList, Function<? super T, V> mapper, Boolean isHandle) {
    //     List<T> list = this.listByIds(idList);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.groupingBy(mapper));
    // }
    //
    // default <V> Map<V, List<T>> queryListMap(Map<String, Object> params, Function<? super T, V> mapper, Boolean isHandle) {
    //     List<T> list = this.list(this.queryWrapper(params));
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.groupingBy(mapper));
    // }
    //
    // default <V> Map<V, List<T>> queryListMap(Wrapper<T> queryWrapper, Function<? super T, V> mapper, Boolean isHandle) {
    //     List<T> list = this.list(queryWrapper);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.groupingBy(mapper));
    // }
    //
    // default <V, A, R> Map<V, R> queryListMap(Collection<? extends Serializable> idList, Function<? super T, V> mapper, Collector<? super T, A, R> collector, Boolean isHandle) {
    //     List<T> list = this.listByIds(idList);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.groupingBy(mapper, collector));
    // }
    //
    // default <V, A, R> Map<V, R> queryListMap(Map<String, Object> params, Function<? super T, V> mapper, Collector<? super T, A, R> collector, Boolean isHandle) {
    //     List<T> list = this.list(this.queryWrapper(params));
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.groupingBy(mapper, collector));
    // }
    //
    // default <V, A, R> Map<V, R> queryListMap(Wrapper<T> queryWrapper, Function<? super T, V> mapper, Collector<? super T, A, R> collector, Boolean isHandle) {
    //     List<T> list = this.list(queryWrapper);
    //     if (isHandle && !list.isEmpty()) {
    //         this.handleList(list, new HashMap<>());
    //     }
    //     return list.stream().collect(Collectors.groupingBy(mapper, collector));
    // }
    //
    // default <V> List<V> queryValueList(Collection<? extends Serializable> idList, Function<? super T, V> mapper) {
    //     return this.listByIds(idList).stream().filter(Objects::nonNull).map(mapper).distinct().collect(Collectors.toList());
    // }
    //
    // default <V> List<V> queryValueList(Map<String, Object> params, Function<? super T, V> mapper) {
    //     return this.list(this.queryWrapper(params)).stream().filter(Objects::nonNull).map(mapper).distinct().collect(Collectors.toList());
    // }
    //
    // default <V> List<V> queryValueList(Wrapper<T> queryWrapper, Function<? super T, V> mapper) {
    //     return this.list(queryWrapper).stream().filter(Objects::nonNull).map(mapper).distinct().collect(Collectors.toList());
    // }
}