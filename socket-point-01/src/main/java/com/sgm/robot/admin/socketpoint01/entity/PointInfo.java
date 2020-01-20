package com.sgm.robot.admin.socketpoint01.entity;

import lombok.Data;

@Data
public class PointInfo {
    private int pointIndex;
    private String pointName;
    private String pointValue;
    private String message;
    private String pointFlag;
    private String flag;
    private String port;
}
