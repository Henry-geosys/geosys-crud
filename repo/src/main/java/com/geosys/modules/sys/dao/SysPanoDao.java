package com.geosys.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geosys.modules.sys.entity.SysPanoEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysPanoDao extends BaseMapper<SysPanoEntity> {
    void insertSysPano(SysPanoEntity paramSysPanoEntity);

    void insertBatch(@Param("list") List<SysPanoEntity> paramList);

    void insertAllColumnsBatch(@Param("list") List<SysPanoEntity> paramList);

    void updateAllColumnsBatch(@Param("list") List<SysPanoEntity> paramList);

    void updatePanoById(SysPanoEntity paramSysPanoEntity);

    void deleteById(Long paramLong);

    SysPanoEntity getPanoById(Long paramLong);
}
