package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.HealthRecord;
import com.community.center.mapper.HealthRecordMapper;
import com.community.center.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HealthServiceImpl implements HealthService {

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Override
    public HealthRecord getHealthRecordById(Long id) {
        return healthRecordMapper.selectById(id);
    }

    @Override
    public List<HealthRecord> getHealthRecordList(Map<String, Object> params) {
        return healthRecordMapper.selectList(params);
    }

    @Override
    public Page<HealthRecord> getHealthRecordPage(Integer page, Integer size, Map<String, Object> params) {
        Page<HealthRecord> pageParam = new Page<>(page, size);
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        
        if (params.get("userId") != null) {
            queryWrapper.eq("user_id", params.get("userId"));
        }
        if (params.get("healthStatus") != null) {
            queryWrapper.eq("health_status", params.get("healthStatus"));
        }
        if (params.get("recordTime") != null) {
            queryWrapper.eq("record_time", params.get("recordTime"));
        }
        if (params.get("startDate") != null) {
            queryWrapper.ge("record_time", params.get("startDate"));
        }
        if (params.get("endDate") != null) {
            queryWrapper.le("record_time", params.get("endDate"));
        }
        
        queryWrapper.orderByDesc("record_time");
        return healthRecordMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public boolean createHealthRecord(HealthRecord healthRecord) {
        // 自动评估健康状态
        evaluateHealthStatus(healthRecord);
        healthRecord.setCreateTime(new Date());
        healthRecord.setUpdateTime(new Date());
        return healthRecordMapper.insert(healthRecord) > 0;
    }

    @Override
    public boolean updateHealthRecord(Long id, HealthRecord healthRecord) {
        healthRecord.setId(id);
        healthRecord.setUpdateTime(new Date());
        return healthRecordMapper.updateById(healthRecord) > 0;
    }

    @Override
    public boolean deleteHealthRecord(Long id) {
        return healthRecordMapper.deleteById(id) > 0;
    }

    @Override
    public List<HealthRecord> getHealthRecordsByUserId(Long userId, Map<String, Object> params) {
        return healthRecordMapper.selectByUserId(userId, params);
    }
    
    @Override
    public boolean deleteHealthRecordsByUserId(Long userId) {
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return healthRecordMapper.delete(queryWrapper) >= 0;
    }
    
    /**
     * 评估健康状态
     * @param healthRecord 健康记录
     */
    private void evaluateHealthStatus(HealthRecord healthRecord) {
        int status = 0; // 默认正常
        
        // BMI判断
        if (healthRecord.getWeight() != null && healthRecord.getHeight() != null && healthRecord.getHeight() > 0) {
            double heightInMeters = healthRecord.getHeight() / 100.0;
            double bmi = healthRecord.getWeight() / (heightInMeters * heightInMeters);
            
            if (bmi < 18.5) {
                status = 1; // 偏瘦
            } else if (bmi >= 28.0) {
                status = 1; // 肥胖
            } else if (bmi >= 24.0) {
                status = 1; // 超重
            }
        }
        
        // 体温判断（默认以腋温为基准）
        if (healthRecord.getTemperature() != null) {
            double temp = healthRecord.getTemperature();
            
            if (temp < 36.0) {
                status = 1; // 体温过低
            } else if (temp > 38.1) {
                status = 1; // 中高热
            } else if (temp >= 37.3) {
                status = 1; // 低热
            }
        }
        
        // 血糖判断
        if (healthRecord.getBloodSugar() != null) {
            double bloodSugar = healthRecord.getBloodSugar();
            
            // 默认按照空腹血糖判断（实际应用中可根据需要添加血糖类型字段）
            if (bloodSugar >= 7.0) {
                status = 1; // 空腹血糖过高
            } else if (bloodSugar >= 6.1) {
                status = 1; // 空腹血糖受损
            }
        }
        
        // 血压判断
        if (healthRecord.getBloodPressure() != null && !healthRecord.getBloodPressure().isEmpty()) {
            String[] bp = healthRecord.getBloodPressure().split("/");
            if (bp.length == 2) {
                try {
                    int high = Integer.parseInt(bp[0].trim());
                    int low = Integer.parseInt(bp[1].trim());
                    
                    if (high >= 160 || low >= 100) {
                        status = 1; // 高血压2级
                    } else if (high >= 140 || low >= 90) {
                        status = 1; // 高血压1级
                    } else if (high < 90 || low < 60) {
                        status = 1; // 低血压
                    } else if (high >= 120 || low >= 80) {
                        status = 1; // 正常高值
                    }
                } catch (NumberFormatException e) {
                    // 血压格式不正确，忽略
                }
            }
        }
        
        // 心率判断
        if (healthRecord.getHeartRate() != null) {
            int heartRate = healthRecord.getHeartRate();
            
            if (heartRate > 100) {
                status = 1; // 心动过速
            } else if (heartRate < 60) {
                status = 1; // 心动过缓
            }
        }
        
        // 血常规判断
        if (healthRecord.getWhiteBloodCell() != null) {
            double wbc = healthRecord.getWhiteBloodCell();
            if (wbc < 3.5 || wbc > 9.5) {
                status = 1; // 白细胞异常
            }
        }
        
        if (healthRecord.getHemoglobin() != null) {
            double hb = healthRecord.getHemoglobin();
            if (hb < 110 || hb > 160) {
                status = 1; // 血红蛋白异常
            }
        }
        
        if (healthRecord.getHematocrit() != null) {
            double hct = healthRecord.getHematocrit();
            if (hct < 35 || hct > 50) {
                status = 1; // 红细胞压积异常
            }
        }
        
        if (healthRecord.getPlateletCount() != null) {
            double plt = healthRecord.getPlateletCount();
            if (plt < 125 || plt > 350) {
                status = 1; // 血小板异常
            }
        }
        
        // 肝功能判断
        if (healthRecord.getAlt() != null) {
            double alt = healthRecord.getAlt();
            if (alt < 7 || alt > 40) {
                status = 1; // 谷丙转氨酶异常
            }
        }
        
        if (healthRecord.getAst() != null) {
            double ast = healthRecord.getAst();
            if (ast < 13 || ast > 35) {
                status = 1; // 谷草转氨酶异常
            }
        }
        
        if (healthRecord.getGgt() != null) {
            double ggt = healthRecord.getGgt();
            if (ggt < 7 || ggt > 60) {
                status = 1; // γ-谷氨酰转移酶异常
            }
        }
        
        if (healthRecord.getAlp() != null) {
            double alp = healthRecord.getAlp();
            if (alp < 45 || alp > 125) {
                status = 1; // 碱性磷酸酶异常
            }
        }
        
        if (healthRecord.getAlbumin() != null) {
            double albumin = healthRecord.getAlbumin();
            if (albumin < 40 || albumin > 55) {
                status = 1; // 白蛋白异常
            }
        }
        
        // 肾功能与尿酸判断
        if (healthRecord.getCreatinine() != null) {
            double creatinine = healthRecord.getCreatinine();
            if (creatinine < 45 || creatinine > 104) {
                status = 1; // 肌酐异常
            }
        }
        
        if (healthRecord.getBloodUreaNitrogen() != null) {
            double bun = healthRecord.getBloodUreaNitrogen();
            if (bun < 3.2 || bun > 7.1) {
                status = 1; // 尿素氮异常
            }
        }
        
        if (healthRecord.getEgfr() != null) {
            double egfr = healthRecord.getEgfr();
            if (egfr < 90) {
                status = 1; // eGFR异常
            }
        }
        
        if (healthRecord.getUricAcid() != null) {
            double ua = healthRecord.getUricAcid();
            if (ua < 155 || ua > 428) {
                status = 1; // 尿酸异常
            }
        }
        
        // 血脂四项判断
        if (healthRecord.getTotalCholesterol() != null) {
            double tc = healthRecord.getTotalCholesterol();
            if (tc >= 5.2) {
                status = 1; // 总胆固醇异常
            }
        }
        
        if (healthRecord.getTriglycerides() != null) {
            double tg = healthRecord.getTriglycerides();
            if (tg >= 1.7) {
                status = 1; // 甘油三酯异常
            }
        }
        
        if (healthRecord.getLdlCholesterol() != null) {
            double ldl = healthRecord.getLdlCholesterol();
            if (ldl >= 3.4) {
                status = 1; // 低密度脂蛋白异常
            }
        }
        
        if (healthRecord.getHdlCholesterol() != null) {
            double hdl = healthRecord.getHdlCholesterol();
            if (hdl < 1.0) {
                status = 1; // 高密度脂蛋白异常
            }
        }
        
        healthRecord.setHealthStatus(status);
    }
}