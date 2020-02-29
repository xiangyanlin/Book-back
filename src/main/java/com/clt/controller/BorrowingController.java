package com.clt.controller;

import com.clt.entity.Borrowing;
import com.clt.service.BorrowingService;
import com.clt.utils.ResultUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Borrowing)表控制层
 *
 * @author makejava
 * @since 2020-02-26 09:33:43
 */
@RestController
@RequestMapping("borrowing")
public class BorrowingController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingController.class);
    /**
     * 服务对象
     */
    @Resource
    private BorrowingService borrowingService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    @ApiOperation("通过主键查询单条数据")
    public ResultUtil<Borrowing> selectOne(
            @ApiParam("id")
            @PathVariable String id) {
        Borrowing borrowing = this.borrowingService.queryById(id);
        if (borrowing != null) {
            return ResultUtil.success(borrowing);
        } else {
            return ResultUtil.failed("没有找到对应信息");
        }
    }

    /**
     * 分页查询数据
     *
     * @param offset 起始
     * @param limit  条数
     * @return 多条数据
     */
    @GetMapping("/limit")
    @ApiOperation("分页查询数据")
    public ResultUtil<List<Borrowing>> selectAllByLimit(
            @ApiParam("起始") Integer offset,
            @ApiParam("条数") Integer limit
    ) {
        offset = (offset == null || offset < 0) ? 0 : offset;
        limit = (limit == null || limit < 0) ? 10 : limit;
        List<Borrowing> borrowings = this.borrowingService.queryAllByLimit(offset, limit);
        if (borrowings != null) {
            return ResultUtil.success(borrowings, "查询成功");
        } else {
            return ResultUtil.failed("查询失败");
        }
    }


    @GetMapping("")
    @ApiOperation("分页查询数据")
    public ResultUtil<PageInfo<Borrowing>> selectAllByPage(
            @ApiParam("页码") @RequestParam(required = false) Integer pageNum,
            @ApiParam("每页大小") @RequestParam(required = false) Integer pageSize
    ) {
        pageNum = (pageNum == null || pageNum < 0) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 0) ? 10 : pageSize;
        PageHelper.startPage(pageNum, pageSize);
        List<Borrowing> typeList = this.borrowingService.queryAll();
        PageInfo<Borrowing> pageInfo = new PageInfo<>(typeList);
        if (pageInfo != null) {
            return ResultUtil.success(pageInfo, "查询成功");
        } else {
            return ResultUtil.failed("查询失败");
        }
    }


    /**
     * 通过实体数据新增单条数据
     *
     * @param borrowing 借阅实体
     * @return 新增的数据
     */
    @PostMapping("")
    public ResultUtil<Borrowing> insert(@RequestBody Borrowing borrowing) {
        Borrowing insertBorrowing = this.borrowingService.insert(borrowing);
        if (insertBorrowing != null) {
            return ResultUtil.success(insertBorrowing, "新增成功");
        } else {
            return ResultUtil.failed("新增失败");
        }
    }

    /**
     * 通过实体数据更新单条数据
     *
     * @param borrowing 借阅实体
     * @return 更新的数据
     */
    @PutMapping("")
    public ResultUtil<Borrowing> update(@RequestBody Borrowing borrowing) {
        if (this.borrowingService.queryById(borrowing.getBookId()) == null){
            return ResultUtil.failed("修改失败，没有找到对应信息");
        }
        Borrowing updateBorrowing = this.borrowingService.update(borrowing);
        if (updateBorrowing != null) {
            return ResultUtil.success(updateBorrowing, "修改成功");
        } else {
            return ResultUtil.failed("修改失败");
        }
    }

    /**
     * 通过主键删除单条数据
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResultUtil<Boolean> delete(@PathVariable String id) {
        if (this.borrowingService.queryById(id) == null){
            return ResultUtil.failed("修改失败，没有找到对应信息");
        }
        boolean flag = this.borrowingService.deleteById(id);
        if (flag) {
            return ResultUtil.success(true, "删除成功");
        } else {
            return ResultUtil.failed("删除失败");
        }
    }

}