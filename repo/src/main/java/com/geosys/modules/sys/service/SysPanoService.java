package com.geosys.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.geosys.modules.sys.entity.SysPanoEntity;
import java.util.List;
import java.util.Map;

public interface SysPanoService extends IService<SysPanoEntity> {
    List<Map<String, String>> readCsv();

    void insertSysPano(SysPanoEntity paramSysPanoEntity);

    void insertPanoFromCsv();

    void updateAllColumnsBatch(List<SysPanoEntity> paramList);

    void updatePanoById(Long paramLong, SysPanoEntity paramSysPanoEntity);

    void deletePanoById(Long paramLong);

    SysPanoEntity getPanoById(Long paramLong);
}
