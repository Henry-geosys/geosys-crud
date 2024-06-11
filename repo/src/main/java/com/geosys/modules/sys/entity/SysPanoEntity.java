package com.geosys.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
@Data
@TableName("sys_pano")
public class SysPanoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long panoId;

    private Long layerId;

    private String panoName;

    private String url;

    private String geom;

    private Float z;

    private Float rotationX;

    private Float rotationY;

    private Float rotationZ;

    private String depthUrl;
}