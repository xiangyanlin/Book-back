package com.clt.service.impl;

import com.clt.dao.BookDao;
import com.clt.dao.BorrowingDao;
import com.clt.dao.UserDao;
import com.clt.entity.Book;
import com.clt.entity.Borrowing;
import com.clt.entity.User;
import com.clt.enums.BookEnum;
import com.clt.enums.BorrowingEnum;
import com.clt.service.BorrowingService;
import com.clt.utils.DateUtils;
import com.clt.utils.ResultUtil;
import com.clt.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (Borrowing)表服务实现类
 *
 * @author makejava
 * @since 2020-02-26 09:33:43
 */
@Service("borrowingService")
public class BorrowingServiceImpl implements BorrowingService {
    @Resource
    private BorrowingDao borrowingDao;

    @Resource
    private BookDao bookDao;

    @Resource
    private UserDao userDao;

    /**
     * 通过ID查询单条数据
     *
     * @param borrowingId 主键
     * @return 实例对象
     */
    @Override
    public Borrowing queryById(String borrowingId) {
        return this.borrowingDao.queryById(borrowingId);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<Borrowing> queryAllByLimit(int offset, int limit) {
        return this.borrowingDao.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param borrowing 实例对象
     * @return 实例对象
     */
    @Override
    public ResultUtil<Borrowing> insert(Borrowing borrowing) {
        if (borrowing == null || StringUtils.isEmpty(borrowing.getBookId()) || StringUtils.isEmpty(borrowing.getBookId())) {
            return ResultUtil.failed("新增借阅书籍信息或用户信息为空");
        } else {
            Book borrowingBook = bookDao.queryById(borrowing.getBookId());
            if (borrowingBook == null) {
                return ResultUtil.failed("没有找到借阅书籍信息");
            }
            User borrowingUser = userDao.queryById(borrowing.getUserId());
            if (borrowingUser == null) {
                return ResultUtil.failed("没有找到借阅人信息");
            }
            Date now = new Date();
            borrowing.setBorrowingId(UUIDUtil.getUUID());
            borrowing.setBorrowingStatus(BorrowingEnum.BORROWING_STATUS_APPLYING.getCode());
            borrowing.setBookName(borrowingBook.getBookName());
            borrowing.setUserName(borrowingUser.getUserName());
            borrowing.setApplicationTime(now);
            borrowing.setCreateTime(now);
            borrowing.setUpdateTime(now);
            borrowingBook.setBookStatus(BookEnum.BOOK_STATUS_LEND.getCode());
            borrowingDao.insert(borrowing);
            bookDao.update(borrowingBook);
            return ResultUtil.success(borrowing,"申请成功");
        }
    }

    /**
     * 修改数据
     *
     * @param borrowing 实例对象
     * @return 实例对象
     */
    @Override
    public Borrowing update(Borrowing borrowing) {
        this.borrowingDao.update(borrowing);
        return this.queryById(borrowing.getBorrowingId());
    }

    /**
     * 通过主键删除数据
     *
     * @param borrowingId 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String borrowingId) {
        return this.borrowingDao.deleteById(borrowingId) > 0;
    }

    @Override
    public List<Borrowing> queryAll() {
        return this.borrowingDao.queryAll();
    }

    @Override
    public List<Borrowing> queryAllByCondition(Borrowing borrowing) {
        List<Borrowing> borrowings = this.borrowingDao.queryAllByCondition(borrowing);
        borrowings = borrowings.stream().map(
                borrowingInfo -> {
                    User user = userDao.queryById(borrowingInfo.getUserId());
                    borrowingInfo.setUser(user);
                    Date now = new Date();
                    long borrowingTime = borrowingInfo.getBorrowingTime().getTime();
                    long overdueDays = ((now.getTime() - borrowingTime) - (borrowingInfo.getDuration() * DateUtils.oneDay)) / DateUtils.oneDay;
                    if (overdueDays > 1 && BorrowingEnum.BORROWING_STATUS_LENT.getCode().equals(borrowingInfo.getBorrowingStatus())){
                        borrowingInfo.setOverdueDays((int)overdueDays);
                    }
                    return borrowingInfo;
                }
        ).collect(Collectors.toList());
        return borrowings;
    }

    @Override
    public ResultUtil<Borrowing> handleApplying(
            String operation, String userName,
            String borrowingId, String note) {
        final Borrowing borrowingResult = queryById(borrowingId);
        if (borrowingResult == null) {
            ResultUtil.failed("没有找到对应借阅信息");
        }
        borrowingResult.setBorrowingStatus(BorrowingEnum.valueOf(operation).getCode());
        if (operation.equals(BorrowingEnum.BORROWING_STATUS_REFUSED.name())){
            Book book = bookDao.queryById(borrowingResult.getBookId());
            book.setBookStatus(BookEnum.BOOK_STATUS_IN_LIBRARY.getCode());
            bookDao.update(book);
        }
        borrowingResult.setNote(note);
        borrowingResult.setBorrowingOperator(userName);
        borrowingResult.setHandleTime(new Date());
        borrowingResult.setUpdateTime(new Date());
        final Borrowing handleResult = update(borrowingResult);
        if (handleResult != null) {
            return ResultUtil.success(handleResult, "处理成功");
        } else {
            return ResultUtil.failed("操作失败");
        }
    }

    @Override
    public ResultUtil<Borrowing> handleReturn(String borrowingId,String userName){
        Borrowing borrowingResult = borrowingDao.queryById(borrowingId);
        if (borrowingResult == null){
            return ResultUtil.failed("没有找到借阅信息");
        } else {
            Date now = new Date();
            long borrowingTime = borrowingResult.getBorrowingTime().getTime();
            if ((now.getTime() - borrowingTime) < (borrowingResult.getDuration() * DateUtils.oneDay)){
                borrowingResult.setBorrowingStatus(BorrowingEnum.BORROWING_STATUS_NORMAL_RETURN.getCode());
            } else {
                borrowingResult.setBorrowingStatus(BorrowingEnum.BORROWING_STATUS_OVERDUE_RETURN.getCode());
            }
            borrowingResult.setReturnOperator(userName);
            borrowingResult.setReturnTime(now);
            borrowingResult.setUpdateTime(now);
            borrowingDao.update(borrowingResult);
            return ResultUtil.success(borrowingResult,"归还成功");
        }
    }

    @Override
    public ResultUtil<Map<String, Object>> getBorrowingStatus() {
        if (BorrowingEnum.values().length <= 0) {
            return ResultUtil.failed("查询信息为空");
        }
        Map<String, Object> data = new HashMap<>(16);
        List<Map<String, String>> dataList = new ArrayList<>(BorrowingEnum.values().length);
        for (BorrowingEnum value : BorrowingEnum.values()) {
            Map<String, String> obj = new HashMap<>(BorrowingEnum.values().length);
            obj.put("message", value.getMessage());
            obj.put("code", value.getCode());
            dataList.add(obj);
        }
        data.put("list", dataList);
        Map<String, String> dataObj = new HashMap<>(16);
        for (BorrowingEnum value : BorrowingEnum.values()) {
            dataObj.put(value.getCode(), value.getMessage());
        }
        data.put("obj", dataObj);
        return ResultUtil.success(data, "查询成功");
    }
}