package com.clt.service.impl;

import com.clt.dao.DictionaryDao;
import com.clt.entity.Dictionary;
import com.clt.entity.DictionaryData;
import com.clt.dao.DictionaryDataDao;
import com.clt.service.DictionaryDataService;
import com.clt.utils.ResultUtil;
import com.clt.utils.UUIDUtil;
import com.mchange.v2.collection.MapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * (DictionaryData)表服务实现类
 *
 * @author makejava
 * @since 2020-03-30 19:07:22
 */
@Service("dictionaryDataService")
public class DictionaryDataServiceImpl implements DictionaryDataService {
    @Resource
    private DictionaryDataDao dictionaryDataDao;

    @Resource
    private DictionaryDao dictionaryDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DictionaryData queryById(String id) {
        return this.dictionaryDataDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<DictionaryData> queryAllByLimit(int offset, int limit) {
        return this.dictionaryDataDao.queryAllByLimit(offset, limit);
    }

    @Override
    public List<DictionaryData> queryAllByCondition(DictionaryData dictionaryData) {
        return this.dictionaryDataDao.queryAllByCondition(dictionaryData);
    }

    private List<DictionaryData> getMajorList(){
        return  dictionaryDataDao.queryMajorList();
    }

    @Override
    public ResultUtil<List<DictionaryData>> getClassInfo() {
        DictionaryData condition = new DictionaryData();
        condition.setStatus(1);
        Map<String,List<DictionaryData>> contain = new HashMap<>(16);
        contain.put("sys_grade", null);
        contain.put("sys_depart", null);
        contain.put("sys_class_number", null);

        for (Map.Entry<String, List<DictionaryData>> data: contain.entrySet()) {
            String key = data.getKey();
            condition.setType(key);
            contain.put(key, queryAllByCondition(condition));
        }

        contain.put("sys_major",getMajorList());

        //把班级编号加入到专业列表信息里面
        contain.get("sys_major").stream().forEach(major -> {
            contain.get("sys_class_number").stream().forEach(classInfo -> {
                major.getChildren().add(classInfo);
            });
        });

        //把专业信息放入院系列表信息中
        contain.get("sys_depart").stream().forEach(depart -> {
            contain.get("sys_major").stream().forEach(major -> {
                if (major.getType().endsWith(depart.getValue())){
                    depart.getChildren().add(major);
                }
            });
        });

        //把院系信息加入到年级列表信息里面
        contain.get("sys_grade").stream().forEach(grade -> {
            contain.get("sys_depart").stream().forEach( depart -> {
                grade.getChildren().add(depart);
            });
        });

        return ResultUtil.success(contain.get("sys_grade"));
    }

    @Override
    public ResultUtil<List<DictionaryData>> getLocationInfo() {
        DictionaryData condition = new DictionaryData();
        condition.setStatus(1);
        Map<String,List<DictionaryData>> contain = new HashMap<>(16);
        contain.put("sys_location_area", null);
        contain.put("sys_location_floor", null);
        contain.put("sys_location_room", null);
        contain.put("sys_location_shelf", null);

        for (Map.Entry<String, List<DictionaryData>> data: contain.entrySet()) {
            String key = data.getKey();
            condition.setType(key);
            contain.put(key, queryAllByCondition(condition));
        }


        //把书架编号加入到房间列表信息里面
        contain.get("sys_location_room").stream().forEach(room -> {
            contain.get("sys_location_shelf").stream().forEach(shelf -> {
                room.getChildren().add(shelf);
            });
        });

        //把房间信息放入楼层列表信息中
        contain.get("sys_location_floor").stream().forEach(floor -> {
            contain.get("sys_location_room").stream().forEach(room -> {
                if (room.getValue().startsWith(floor.getValue())){
                    floor.getChildren().add(room);
                }
            });
        });

        //把楼层信息加入到楼区列表信息里面
        contain.get("sys_location_area").stream().forEach(area -> {
            contain.get("sys_location_floor").stream().forEach( floor -> {
                area.getChildren().add(floor);
            });
        });

        return ResultUtil.success(contain.get("sys_location_area"));
    }

    /**
     * 新增数据
     *
     * @param dictionaryData 实例对象
     * @return 实例对象
     */
    @Override
    public DictionaryData insert(DictionaryData dictionaryData) {
        if (dictionaryData.getId() == null){
            dictionaryData.setId(UUIDUtil.getUUID());
        }
        if (dictionaryData.getSort() == null){
            dictionaryData.setSort(0);
        }
        if (dictionaryData.getStatus() == null){
            dictionaryData.setStatus(1);
        }
        dictionaryData.setCreateTime(new Date());
        this.dictionaryDataDao.insert(dictionaryData);
        return dictionaryData;
    }

    /**
     * 修改数据
     *
     * @param dictionaryData 实例对象
     * @return 实例对象
     */
    @Override
    public DictionaryData update(DictionaryData dictionaryData) {
        this.dictionaryDataDao.update(dictionaryData);
        return this.queryById(dictionaryData.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return this.dictionaryDataDao.deleteById(id) > 0;
    }

    /**
     * 删除某一字典类型的所有字典数据
     *
     * @param type 字典类型名称
     * @return 是否成功
     */
    @Override
    public ResultUtil<Integer> deleteByType(String type) {
        return ResultUtil.success(this.dictionaryDataDao.deleteByType(type));
    }

    @Autowired
    private StringRedisTemplate template;

    @Override
    public ResultUtil<Map<Object, Object>> insert(String field, String key, String value) {
        if (field == null) {
            final String valueResult = template.opsForValue().get(key);
            if (valueResult != null && valueResult.trim().length() != 0) {
                ResultUtil.failed("新增失败，编号重复。");
            } else {
                template.opsForValue().set(key, value);
            }
        } else {
            final String valueResult = (String) template.opsForHash().get(field, key);
            if (valueResult != null && valueResult.trim().length() != 0) {
                ResultUtil.failed("新增失败，编号重复。");
            } else {
                template.opsForHash().put(field, key, value);
            }
        }
        return ResultUtil.success(null, "新增成功");
    }

    @Override
    public ResultUtil<Map<Object, Object>> delete(String field, String key) {
        if (field != null && key != null) {
            template.opsForHash().delete(field, key);
        }
        if (field == null) {
            template.delete(key);
        } else {
            template.delete(field);
        }
        return ResultUtil.success(null, "删除成功");
    }



    @Override
    public ResultUtil<Map<Object, Object>> update(String field, String key, String value) {
        if (field == null) {
            template.opsForValue().set(key, value);
        } else {
            template.opsForHash().put(field, key, value);
        }
        return ResultUtil.success(null, "更新成功");
    }

    @Override
    public ResultUtil<Map<Object, Object>> query(String field, String key) {
        Map<Object, Object> result = new HashMap<>();
        if (field != null && key != null) {
            result.put("result", template.opsForHash().get(field, key));
        }
        if (field == null) {
            result.put("result", template.opsForValue().get(key));
        } else {
            return ResultUtil.success(template.opsForHash().entries(field), "查询成功");
        }
        return ResultUtil.success(result, "查询成功");
    }

    @Override
    public ResultUtil< List<Map<Object, Object>>> multipleQuery(List<String> fields) {
        List<Map<Object, Object>> result = new ArrayList<>();
        fields.stream().forEach( field -> {
            result.add(template.opsForHash().entries(field));
        });
        return  ResultUtil.success(result, "查询成功");
    }
}