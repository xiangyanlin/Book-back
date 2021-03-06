package com.clt.dao;

import com.clt.entity.Borrowing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Borrowing)表数据库访问层
 *
 * @author makejava
 * @since 2020-02-26 09:33:37
 */
@Mapper
public interface BorrowingDao {

    /**
     * 通过ID查询单条数据
     *
     * @param borrowingId 主键
     * @return 实例对象
     */
    Borrowing queryById(String borrowingId);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Borrowing> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    List<Borrowing> queryAll();

    /**
     * 通过实体作为筛选条件查询
     *
     * @param borrowing 实例对象
     * @return 对象列表
     */
    List<Borrowing> queryAllByCondition(Borrowing borrowing);


    /**
     * 新增数据
     *
     * @param borrowing 实例对象
     * @return 影响行数
     */
    int insert(Borrowing borrowing);

    /**
     * 修改数据
     *
     * @param borrowing 实例对象
     * @return 影响行数
     */
    int update(Borrowing borrowing);

    /**
     * 通过主键删除数据
     *
     * @param borrowingId 主键
     * @return 影响行数
     */
    int deleteById(String borrowingId);

}