package com.community.center.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 健康档案实体类
 * 用于存储用户的健康检查记录
 */
@Data
@TableName("health_record")
public class HealthRecord {
    @TableId
    private Long id;
    @TableField("user_id")
    private Long userId;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("record_time")
    private Date recordTime;
    @TableField("blood_pressure")
    private String bloodPressure;
    @TableField("temperature")
    private Double temperature;
    @TableField("blood_sugar")
    private Double bloodSugar;
    @TableField("heart_rate")
    private Integer heartRate;
    @TableField("weight")
    private Double weight;
    @TableField("height")
    private Double height;
    @TableField("health_status")
    private Integer healthStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private Date updateTime;
    @TableField("remark")
    private String remark;

    @TableField("white_blood_cell")
    private Double whiteBloodCell;
    @TableField("hemoglobin")
    private Double hemoglobin;
    @TableField("hematocrit")
    private Double hematocrit;
    @TableField("platelet_count")
    private Double plateletCount;
    @TableField("alt")
    private Double alt;
    @TableField("ast")
    private Double ast;
    @TableField("ggt")
    private Double ggt;
    @TableField("alp")
    private Double alp;
    @TableField("total_protein")
    private Double totalProtein;
    @TableField("albumin")
    private Double albumin;
    @TableField("creatinine")
    private Double creatinine;
    @TableField("blood_urea_nitrogen")
    private Double bloodUreaNitrogen;
    @TableField("egfr")
    private Double egfr;
    @TableField("uric_acid")
    private Double uricAcid;
    @TableField("total_cholesterol")
    private Double totalCholesterol;
    @TableField("triglycerides")
    private Double triglycerides;
    @TableField("ldl_cholesterol")
    private Double ldlCholesterol;
    @TableField("hdl_cholesterol")
    private Double hdlCholesterol;
}
