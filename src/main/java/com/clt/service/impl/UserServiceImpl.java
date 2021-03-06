package com.clt.service.impl;

import com.clt.constant.Const;
import com.clt.dao.PermissionDao;
import com.clt.dao.UserClassDao;
import com.clt.dao.UserDao;
import com.clt.entity.Email;
import com.clt.entity.Permission;
import com.clt.entity.User;
import com.clt.entity.UserClass;
import com.clt.enums.UserEnum;
import com.clt.service.UserClassService;
import com.clt.service.UserService;
import com.clt.utils.DateUtils;
import com.clt.utils.MailUtil;
import com.clt.utils.ResultUtil;
import com.clt.utils.UUIDUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (User)表服务实现类
 *
 * @author makejava
 * @since 2020-02-25 19:05:15
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;

    @Resource
    private PermissionDao permissionDao;

    @Resource
    private UserClassService userClassService;

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private MailUtil mailUtil;

    /**
     * 通过ID查询单条数据
     *
     * @param userId 主键
     * @return 实例对象
     */
    @Override
    public User queryById(String userId) {
        return this.userDao.queryById(userId);
    }

    @Override
    public User queryByUserName(String userName) {
        return userDao.queryByUserName(userName);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<User> queryAllByLimit(int offset, int limit) {
        return this.userDao.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    @Override
    public User insert(User user) {
        beforeInsertUser(user);
        final Permission defaultPermission = new Permission();
        defaultPermission.setUserId(user.getUserId());
        if (UserEnum.USER_ROLE_ADMIN.getCode().equals(user.getRole())) {
            defaultPermission.setAdmin(true);
        }
        permissionDao.insert(defaultPermission);
        this.userDao.insert(user);
        return user;
    }

    private void beforeInsertUser(User user) {
        if (user.getStuNo() == null || StringUtils.isEmpty(user.getStuNo())) {
            user.setStuNo(UUIDUtil.getUUID());
        }
        if (user.getStatus() == null || StringUtils.isEmpty(user.getStatus())) {
            user.setStatus(UserEnum.USER_STATUS_NORMAL.getCode());
        }
        if (user.getRole() == null || StringUtils.isEmpty(user.getRole())) {
            user.setRole(UserEnum.USER_ROLE_STUDENT.getCode());
        }
        if (user.getSex() == null || StringUtils.isEmpty(user.getSex())) {
            user.setSex(UserEnum.USER_SEX_MALE.getCode());
        }
        if (user.getCredit() == null || user.getCredit() < 0 || user.getCredit() > 100) {
            user.setCredit(60);
        }
        if (user.getClassId() != null){
            UserClass userClass = new UserClass();
            userClass.setClassId(user.getClassId());
            userClassService.insert(userClass);
        }
        user.setAvatar(Const.USER_DEFAULT_AVATAR);
        user.setUserId(user.getStuNo());
        user.setPassword(Const.INITIAL_PASSWORD);
        Object md5PassWord = new SimpleHash(Const.ENCRYPTION_ALGORITHM, user.getPassword(),
                user.getUserName(), Const.ENCRYPTION_TIMES);
        user.setPassword(md5PassWord.toString());
        Date now = new Date();
        user.setRegisterTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
    }


    /**
     * 修改数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    @Override
    public User update(User user) {
        if (user.getClassId() != null){
            UserClass userClass = new UserClass();
            userClass.setClassId(user.getClassId());
            userClassService.insert(userClass);
        }
        this.userDao.update(user);
        return this.queryById(user.getUserId());
    }

    /**
     * 通过主键删除数据
     *
     * @param userId 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String userId) {
        return this.userDao.deleteById(userId) > 0;
    }

    @Override
    public List<User> queryAllByCondition(User user) {
        List<User> users = this.userDao.queryAllByCondition(user).stream().map(userResult -> {
            if (userResult.getCredit() != null) {
                userResult.setCreditStars(userResult.getCredit() / 20);
            }
            return userResult;
        }).collect(Collectors.toList());
        return users;
    }

    @Override
    public List<User> queryByClass(UserClass userClass) {
        return userDao.queryByClass(userClass);
    }

    @Override
    public ResultUtil<Map<String, Object>> sendVerification(User user, String operation) {
        Random random = new Random();
        Map<String, Object> rmap = new HashMap<>(16);
        Map<String, Object> map = new HashMap<>(16);
        String content = "";
        int retime = DateUtils.remainingTime();
        for (int i = 0; i < 6; i++) {
            int temp = random.nextInt(10);
            content += temp;
        }
        if (operation == null) {
            operation = "";
        }
        String key = user.getEmail() + operation + "check";
        String check = template.opsForValue().get(key);
        if (check != null) {
            if (Integer.parseInt(check) >= 3) {
                template.expire(key, retime, TimeUnit.SECONDS);
                return ResultUtil.failed("您今天已连续发送三次验证码，账号今天已锁定");
            } else {
                template.opsForValue().increment(key);
            }
        } else {
            template.opsForValue().set(key, "1");
        }
        rmap.put(operation + "password", content);
        rmap.put("count", "0");
        template.opsForHash().putAll(user.getEmail(), rmap);
        mailUtil.sendSimpleMail(new Email(user.getEmail(), "验证码邮件", "验证码为: " + content + " 五分钟内有效"));
        template.expire(user.getEmail(), 300, TimeUnit.SECONDS);
        return ResultUtil.success(map, "信息发送成功");
    }

    @Override
    public ResultUtil<Map<String, Object>> verificationCheck(User user, String operation) {
        Map<String, Object> map = new HashMap<>(16);
        String temp;
        if (operation == null) {
            operation = "";
        }
        String password = (String) template.opsForHash().get(user.getEmail(), operation + "password");
        if (password != null) {
            temp = (String) template.opsForHash().get(user.getEmail(), "count");
            if (Integer.parseInt(temp) >= 2) {
                template.delete(user.getEmail());
                return ResultUtil.failed("您已连续输错三次，验证码已失效");
            } else if (user.getPassword().equals(password)) {
                template.opsForValue().set(user.getEmail() + operation + "check", "0");
                return ResultUtil.success(null, "验证成功");
            } else {
                template.opsForHash().increment(user.getEmail(), "count", 1);
                return ResultUtil.failed("验证失败");
            }
        } else {
            return ResultUtil.failed("请输入验证码");
        }
    }


    @Override
    public ResultUtil<Map<String, Object>> updatePWByOldPW(String oldPassword, String newPassword, String userId) {
        User userResult = this.userDao.queryById(userId);
        if (userResult == null) {
            return ResultUtil.failed("没有找到用户信息，修改失败");
        }
        Object md5OldPassword = new SimpleHash(Const.ENCRYPTION_ALGORITHM, oldPassword,
                userResult.getUserName(), Const.ENCRYPTION_TIMES);
        if (!userResult.getPassword().equals(md5OldPassword.toString())) {
            return ResultUtil.failed("旧密码校验未通过，修改失败");
        }
        Object md5NewPassword = new SimpleHash(Const.ENCRYPTION_ALGORITHM, newPassword,
                userResult.getUserName(), Const.ENCRYPTION_TIMES);
        userResult.setPassword(md5NewPassword.toString());
        this.userDao.update(userResult);
        return ResultUtil.success(null, "修改成功");
    }

    @Override
    public ResultUtil<Map<String, Object>> updatePWByVerificationCode(String userId, String newPassword) {
        if (userId == null) {
            return ResultUtil.failed("用户id为空");
        }
        final User userResult = this.userDao.queryById(userId);
        if (userResult == null) {
            return ResultUtil.failed("没有找到用户信息");
        }
        Object md5NewPassword = new SimpleHash(Const.ENCRYPTION_ALGORITHM, newPassword,
                userResult.getUserName(), Const.ENCRYPTION_TIMES);
        userResult.setPassword(md5NewPassword.toString());
        this.userDao.update(userResult);
        return ResultUtil.success(null, "密码修改成功");
    }
}