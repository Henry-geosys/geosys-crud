package com.geosys.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geosys.modules.sys.dao.SysDemoDao;
import com.geosys.modules.sys.entity.SysDemoEntity;
import com.geosys.modules.sys.service.SysDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("SysDemoService")
public class SysDemoServiceImpl extends ServiceImpl<SysDemoDao, SysDemoEntity>  implements SysDemoService {

    @Autowired
    private SysDemoDao sysdemoDao;

}