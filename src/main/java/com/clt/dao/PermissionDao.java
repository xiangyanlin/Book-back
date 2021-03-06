package com.clt.dao;

import com.clt.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (Permission)表数据库访问层
 *
 * @author makejava
 * @since 2020-02-26 09:39:14
 */
@Mapper
public interface PermissionDao {

    /**
     * 通过ID查询单条数据
     *
     * @param userId 主键
     * @return 实例对象
     */
    Permission queryById(String userId);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Permission> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param permission 实例对象
     * @return 对象列表
     */
    List<Permission> queryAllByCondition(Permission permission);

    /**
     * 新增数据
     *
     * @param permission 实例对象
     * @return 影响行数
     */
    int insert(Permission permission);

    /**
     * 新增数据
     *
     * @param permissionList 实例对象
     * @return 影响行数
     */
    int insertBatch(List<Permission> permissionList);

    /**
     * 修改数据
     *
     * @param permission 实例对象
     * @return 影响行数
     */
    int update(Permission permission);

    /**
     * 通过主键删除数据
     *
     * @param userId 主键
     * @return 影响行数
     */
    int deleteById(String userId);


    List<Permission> queryAll();
}