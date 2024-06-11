package com.geosys.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
@Data
@TableName("sys_demo2")
public class SysDemoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Float value;

}